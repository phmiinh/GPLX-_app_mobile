package com.example.afinal;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fallback recommendation engine when API is unavailable.
 * Uses heuristic based on recent wrong answers, critical questions, and basic due logic.
 */
public class SmartStudyEngine {
    private static final String TAG = "SmartStudyEngine";
    private static final long DAY_MS = 24 * 60 * 60 * 1000L;
    private static final long THIRTY_DAYS_MS = 30 * DAY_MS;
    private static final long SEVEN_DAYS_MS = 7 * DAY_MS;

    public interface FallbackCallback {
        void onSuccess(List<QuestionRecommendation> recommendations);
        void onError(Exception e);
    }

    public static class QuestionRecommendation {
        public String questionId;
        public int topicId;
        public boolean isCritical;
        public double priorityScore;
        public List<String> reasons;
        public double predictedCorrectProb;
        public long dueTimeMs;

        public QuestionRecommendation(String questionId, int topicId, boolean isCritical) {
            this.questionId = questionId;
            this.topicId = topicId;
            this.isCritical = isCritical;
            this.reasons = new ArrayList<>();
            this.predictedCorrectProb = 0.5; // Default
            this.dueTimeMs = 0;
        }
    }

    /**
     * Get fallback recommendations based on recent attempts
     */
    public static void getFallbackRecommendations(
            String userId,
            Integer topicId,
            int numQuestions,
            boolean criticalOnly,
            FallbackCallback callback
    ) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        long now = System.currentTimeMillis();
        long thirtyDaysAgo = now - THIRTY_DAYS_MS;

        // Query attempts from last 30 days
        // Note: Simplified query to avoid index requirement - only filter by user_id, no orderBy
        Query query = db.collection("attempts")
                .whereEqualTo("user_id", userId)
                .limit(1000); // Reasonable limit

        query.get().addOnSuccessListener((QuerySnapshot snapshot) -> {
            try {
                // Process attempts to build statistics
                Map<String, QuestionStats> questionStats = new HashMap<>();
                Map<String, Long> lastSeenMap = new HashMap<>();
                Map<Integer, TopicStats> topicStats = new HashMap<>();

                // Sort by timestamp in code (to avoid index requirement)
                List<QueryDocumentSnapshot> docs = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshot) {
                    long timestamp = doc.getLong("timestamp_ms") != null ? doc.getLong("timestamp_ms") : 0;
                    // Filter by timestamp in code
                    if (timestamp >= thirtyDaysAgo) {
                        docs.add(doc);
                    }
                }
                
                // Sort by timestamp descending
                Collections.sort(docs, new Comparator<QueryDocumentSnapshot>() {
                    @Override
                    public int compare(QueryDocumentSnapshot a, QueryDocumentSnapshot b) {
                        long tsA = a.getLong("timestamp_ms") != null ? a.getLong("timestamp_ms") : 0;
                        long tsB = b.getLong("timestamp_ms") != null ? b.getLong("timestamp_ms") : 0;
                        return Long.compare(tsB, tsA); // Descending
                    }
                });
                
                for (QueryDocumentSnapshot doc : docs) {
                    String qid = doc.getString("question_id");
                    int tid = doc.getLong("topic_id") != null ? doc.getLong("topic_id").intValue() : 0;
                    boolean correct = Boolean.TRUE.equals(doc.getBoolean("is_correct"));
                    long timestamp = doc.getLong("timestamp_ms") != null ? doc.getLong("timestamp_ms") : 0;

                    // Update question stats
                    QuestionStats stats = questionStats.getOrDefault(qid, new QuestionStats());
                    stats.totalAttempts++;
                    if (!correct) {
                        stats.wrongCount++;
                    }
                    questionStats.put(qid, stats);

                    // Track last seen
                    if (timestamp > lastSeenMap.getOrDefault(qid, 0L)) {
                        lastSeenMap.put(qid, timestamp);
                    }

                    // Update topic stats
                    TopicStats tStats = topicStats.getOrDefault(tid, new TopicStats());
                    tStats.totalAttempts++;
                    if (!correct) {
                        tStats.wrongCount++;
                    }
                    topicStats.put(tid, tStats);
                }

                // Get question meta for all questions
                db.collection("question_meta")
                        .get()
                        .addOnSuccessListener((QuerySnapshot metaSnapshot) -> {
                            List<QuestionRecommendation> recommendations = new ArrayList<>();

                            for (QueryDocumentSnapshot metaDoc : metaSnapshot) {
                                String qid = metaDoc.getId();
                                int tid = metaDoc.getLong("topic_id") != null ? metaDoc.getLong("topic_id").intValue() : 0;
                                boolean isCritical = Boolean.TRUE.equals(metaDoc.getBoolean("is_critical"));

                                // Filter by topic if specified
                                if (topicId != null && tid != topicId) {
                                    continue;
                                }

                                // Filter by critical if specified
                                if (criticalOnly && !isCritical) {
                                    continue;
                                }

                                QuestionRecommendation rec = new QuestionRecommendation(qid, tid, isCritical);
                                QuestionStats stats = questionStats.getOrDefault(qid, new QuestionStats());
                                long lastSeen = lastSeenMap.getOrDefault(qid, 0L);

                                // Calculate priority score
                                double priority = 0.0;

                                // Reason 1: Wrong multiple times
                                if (stats.wrongCount >= 2) {
                                    priority += 0.4;
                                    rec.reasons.add("Sai nhiều lần");
                                } else if (stats.wrongCount >= 1) {
                                    priority += 0.2;
                                    rec.reasons.add("Sai gần đây");
                                }

                                // Reason 2: Critical + wrong
                                if (isCritical && stats.wrongCount >= 1) {
                                    priority += 0.3;
                                    rec.reasons.add("Câu liệt");
                                } else if (isCritical) {
                                    priority += 0.15;
                                    rec.reasons.add("Câu liệt cần ôn");
                                }

                                // Reason 3: Not seen in 7+ days
                                if (lastSeen == 0) {
                                    priority += 0.2;
                                    rec.reasons.add("Chưa từng làm");
                                } else {
                                    long daysSinceSeen = (now - lastSeen) / DAY_MS;
                                    if (daysSinceSeen > 7) {
                                        priority += 0.15;
                                        rec.reasons.add("Đến hạn ôn");
                                        rec.dueTimeMs = lastSeen + SEVEN_DAYS_MS;
                                    }
                                }

                                // Reason 4: Weak topic
                                TopicStats tStats = topicStats.getOrDefault(tid, new TopicStats());
                                if (tStats.totalAttempts >= 5) {
                                    double topicAccuracy = 1.0 - ((double) tStats.wrongCount / tStats.totalAttempts);
                                    if (topicAccuracy < 0.5) {
                                        priority += 0.1;
                                        rec.reasons.add("Chủ đề yếu");
                                    }
                                }

                                rec.priorityScore = priority;
                                rec.predictedCorrectProb = stats.totalAttempts > 0
                                        ? 1.0 - ((double) stats.wrongCount / stats.totalAttempts)
                                        : 0.5;

                                if (priority > 0) {
                                    recommendations.add(rec);
                                }
                            }

                            // Sort by priority score descending
                            recommendations.sort((a, b) -> Double.compare(b.priorityScore, a.priorityScore));

                            // Return top N
                            int limit = Math.min(numQuestions, recommendations.size());
                            callback.onSuccess(recommendations.subList(0, limit));

                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Error fetching question meta", e);
                            callback.onError(e);
                        });

            } catch (Exception e) {
                Log.e(TAG, "Error processing fallback recommendations", e);
                callback.onError(e);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching attempts", e);
            callback.onError(e);
        });
    }

    private static class QuestionStats {
        int totalAttempts = 0;
        int wrongCount = 0;
    }

    private static class TopicStats {
        int totalAttempts = 0;
        int wrongCount = 0;
    }
}

