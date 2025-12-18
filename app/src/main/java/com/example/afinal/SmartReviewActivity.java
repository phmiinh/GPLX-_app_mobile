package com.example.afinal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.afinal.adapter.SmartReviewAdapter;
import com.example.afinal.analytics.AnalyticsHelper;
import com.example.afinal.analytics.UserIdentity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Screen showing due questions organized by urgency:
 * - Đến hạn hôm nay (due within 24h)
 * - Đến hạn 24h (due within 48h)
 * - Nguy cơ sai cao (p_correct < 0.6)
 * - 60 câu liệt cần ưu tiên
 */
public class SmartReviewActivity extends AppCompatActivity {
    private static final String TAG = "SmartReviewActivity";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyState;
    private Button btnStartPractice;

    private AiRecommendationClient client;
    private SmartReviewAdapter adapter;
    private List<DueQuestionItem> allItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_smart_review);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        client = new AiRecommendationClient();
        
        // Initialize Analytics
        AnalyticsHelper.initialize(this);
        AnalyticsHelper.logAISmartReviewOpened(this);
        
        bindViews();
        setupRecyclerView();
        loadDueList();
    }

    private void bindViews() {
        recyclerView = findViewById(R.id.rv_due_list);
        progressBar = findViewById(R.id.pb_loading);
        emptyState = findViewById(R.id.txt_empty_state);
        btnStartPractice = findViewById(R.id.btn_start_practice);
        
        btnStartPractice.setOnClickListener(v -> {
            Intent intent = new Intent(this, SmartPracticeActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        adapter = new SmartReviewAdapter(new ArrayList<>(), new SmartReviewAdapter.OnQuestionClickListener() {
            @Override
            public void onQuestionClick(String questionId) {
                // Find question metadata for analytics
                for (DueQuestionItem item : allItems) {
                    if (!item.isSectionHeader && questionId != null && questionId.equals(item.questionId)) {
                        AnalyticsHelper.logAIRecommendationClicked(
                                SmartReviewActivity.this, questionId, item.topicId, item.isCritical, item.predictedCorrectProb
                        );
                        break;
                    }
                }
                
                // Navigate to question detail or start practice with this question
                Intent intent = new Intent(SmartReviewActivity.this, SmartPracticeActivity.class);
                intent.putExtra("question_id", questionId);
                startActivity(intent);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadDueList() {
        setLoading(true);
        emptyState.setVisibility(View.GONE);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = firebaseUser != null ? firebaseUser.getUid() : UserIdentity.getUserId(this);

        // Request recommendations for all topics, prioritize due/weak questions
        client.requestRecommendationsWithRetry(
                this,
                "ai_practice",
                null, // All topics
                100, // Get more to categorize
                false,
                new AiRecommendationClient.RecommendationCallback() {
                    @Override
                    public void onSuccess(List<String> ids, List<Map<String, Object>> metadata) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            if (ids == null || ids.isEmpty()) {
                                // Try fallback
                                loadFallbackDueList(userId);
                                return;
                            }

                            categorizeAndDisplay(metadata);
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.w(TAG, "API failed, using fallback", e);
                        runOnUiThread(() -> loadFallbackDueList(userId));
                    }
                }
        );
    }

    private void loadFallbackDueList(String userId) {
        SmartStudyEngine.getFallbackRecommendations(userId, null, 100, false, new SmartStudyEngine.FallbackCallback() {
            @Override
            public void onSuccess(List<SmartStudyEngine.QuestionRecommendation> recommendations) {
                runOnUiThread(() -> {
                    setLoading(false);
                    if (recommendations == null || recommendations.isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    // Convert to metadata format
                    List<Map<String, Object>> metadata = new ArrayList<>();
                    for (SmartStudyEngine.QuestionRecommendation rec : recommendations) {
                        Map<String, Object> meta = new HashMap<>();
                        meta.put("question_id", rec.questionId);
                        meta.put("topic_id", rec.topicId);
                        meta.put("is_critical", rec.isCritical);
                        meta.put("predicted_correct_prob", rec.predictedCorrectProb);
                        meta.put("urgency_score", rec.dueTimeMs > 0 ? 0.5 : 0.0);
                        meta.put("due_time_ms", rec.dueTimeMs);
                        meta.put("reasons", rec.reasons);
                        metadata.add(meta);
                    }
                    categorizeAndDisplay(metadata);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showEmptyState();
                });
            }
        });
    }

    private void categorizeAndDisplay(List<Map<String, Object>> metadata) {
        long now = System.currentTimeMillis();
        long oneDayMs = 24 * 60 * 60 * 1000L;
        long twoDaysMs = 2 * oneDayMs;

        List<DueQuestionItem> todayDue = new ArrayList<>();
        List<DueQuestionItem> soonDue = new ArrayList<>();
        List<DueQuestionItem> highRisk = new ArrayList<>();
        List<DueQuestionItem> critical = new ArrayList<>();

        for (Map<String, Object> meta : metadata) {
            String qid = (String) meta.get("question_id");
            if (qid == null) continue;

            boolean isCritical = Boolean.TRUE.equals(meta.get("is_critical"));
            double pCorrect = ((Number) meta.getOrDefault("predicted_correct_prob", 0.5)).doubleValue();
            double urgency = ((Number) meta.getOrDefault("urgency_score", 0.0)).doubleValue();
            long dueTime = ((Number) meta.getOrDefault("due_time_ms", 0L)).longValue();
            @SuppressWarnings("unchecked")
            List<String> reasons = (List<String>) meta.getOrDefault("reasons", new ArrayList<>());

            DueQuestionItem item = new DueQuestionItem();
            item.questionId = qid;
            item.topicId = ((Number) meta.getOrDefault("topic_id", 0)).intValue();
            item.isCritical = isCritical;
            item.predictedCorrectProb = pCorrect;
            item.urgencyScore = urgency;
            item.dueTimeMs = dueTime;
            item.reasons = reasons != null ? reasons : new ArrayList<>();

            // Categorize
            if (dueTime > 0 && dueTime <= now + oneDayMs) {
                todayDue.add(item);
            } else if (dueTime > 0 && dueTime <= now + twoDaysMs) {
                soonDue.add(item);
            }

            if (pCorrect < 0.6 || urgency > 0.5) {
                highRisk.add(item);
            }

            if (isCritical && (pCorrect < 0.7 || urgency > 0.3)) {
                critical.add(item);
            }
        }

        // Build display list with sections (avoid duplicates)
        allItems.clear();
        java.util.Set<String> addedIds = new java.util.HashSet<>();
        
        if (!todayDue.isEmpty()) {
            allItems.add(new DueQuestionItem("Đến hạn hôm nay", todayDue.size()));
            for (DueQuestionItem item : todayDue) {
                if (!addedIds.contains(item.questionId)) {
                    allItems.add(item);
                    addedIds.add(item.questionId);
                }
            }
        }
        if (!soonDue.isEmpty()) {
            allItems.add(new DueQuestionItem("Đến hạn 24h", soonDue.size()));
            for (DueQuestionItem item : soonDue) {
                if (!addedIds.contains(item.questionId)) {
                    allItems.add(item);
                    addedIds.add(item.questionId);
                }
            }
        }
        if (!highRisk.isEmpty()) {
            allItems.add(new DueQuestionItem("Nguy cơ sai cao", highRisk.size()));
            for (DueQuestionItem item : highRisk) {
                if (!addedIds.contains(item.questionId)) {
                    allItems.add(item);
                    addedIds.add(item.questionId);
                }
            }
        }
        if (!critical.isEmpty()) {
            allItems.add(new DueQuestionItem("60 câu liệt cần ưu tiên", critical.size()));
            for (DueQuestionItem item : critical) {
                if (!addedIds.contains(item.questionId)) {
                    allItems.add(item);
                    addedIds.add(item.questionId);
                }
            }
        }

        if (allItems.isEmpty()) {
            showEmptyState();
        } else {
            adapter.updateItems(allItems);
            recyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        emptyState.setText("Chưa có câu hỏi đến hạn. Hãy làm thêm vài đề để AI có thể đề xuất!");
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    public static class DueQuestionItem {
        public String questionId;
        public int topicId;
        public boolean isCritical;
        public double predictedCorrectProb;
        public double urgencyScore;
        public long dueTimeMs;
        public List<String> reasons;

        // For section headers
        public String sectionTitle;
        public int sectionCount;
        public boolean isSectionHeader;

        public DueQuestionItem() {
            this.reasons = new ArrayList<>();
            this.isSectionHeader = false;
        }

        public DueQuestionItem(String title, int count) {
            this.sectionTitle = title;
            this.sectionCount = count;
            this.isSectionHeader = true;
        }
    }
}

