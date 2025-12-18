package com.example.afinal;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Màn hình Home/Dashboard chính với Nomadstay design:
 * - Hero dark card với greeting + search
 * - Category chips
 * - Quick access cards
 * - Stats với charts (Line, Pie)
 * - Bottom bar + FAB
 */
public class DashboardActivity extends BaseNavigationActivity {

    private TextView greetingTitle;
    private TextView greetingSubtitle;
    private TextView statsHint;
    private TextView txtStatExamCount;
    private TextView txtStatBestScore;
    private TextView txtStatAvgScore;
    private ImageButton btnMenu;
    private ImageButton btnAvatar;
    private EditText edtSearch;
    
    // Quick access cards
    private MaterialCardView cardBookmarks;
    private MaterialCardView cardHistory;
    private MaterialCardView cardLeaderboard;
    private MaterialCardView cardSmart;
    
    // Charts
    private LineChart lineChartScores;
    private PieChart pieChartCorrect;
    
    // Category chips
    private ChipGroup chipGroupCategories;
    private Chip chipPractice;
    private Chip chipMockExam;
    private Chip chipAI;
    private Chip chipCritical;
    private Chip chipWrongOften;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_dashboard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();
        
        // Setup navigation
        enableDrawer();
        enableBottomBar(0); // Home tab selected
        
        // Setup menu button click
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(androidx.core.view.GravityCompat.START);
                }
            });
        }
        db = FirebaseFirestore.getInstance();
        setupGreeting();
        setupChips();
        setupCards();
        setupCharts();
        loadStatsIfLoggedIn();
    }
    
    @Override
    protected void onFabClick() {
        // Override FAB behavior - navigate to Smart Practice instead of Level
        startActivity(new Intent(this, SmartPracticeActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload stats when returning to dashboard
        loadStatsIfLoggedIn();
    }

    private void bindViews() {
        greetingTitle = findViewById(R.id.txtDashboardTitle);
        greetingSubtitle = findViewById(R.id.txtDashboardSubtitle);
        statsHint = findViewById(R.id.txtDashboardStatsHint);
        txtStatExamCount = findViewById(R.id.txtStatExamCount);
        txtStatBestScore = findViewById(R.id.txtStatBestScore);
        txtStatAvgScore = findViewById(R.id.txtStatAvgScore);
        btnMenu = findViewById(R.id.btnMenuMain);
        btnAvatar = findViewById(R.id.btnDashboardAvatar);
        edtSearch = findViewById(R.id.edtSearch);
        
        cardBookmarks = findViewById(R.id.cardBookmarks);
        cardHistory = findViewById(R.id.cardHistory);
        cardLeaderboard = findViewById(R.id.cardLeaderboard);
        cardSmart = findViewById(R.id.cardSmart);
        
        lineChartScores = findViewById(R.id.lineChartScores);
        pieChartCorrect = findViewById(R.id.pieChartCorrect);
        
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        chipPractice = findViewById(R.id.chipPractice);
        chipMockExam = findViewById(R.id.chipMockExam);
        chipAI = findViewById(R.id.chipAI);
        chipCritical = findViewById(R.id.chipCritical);
        chipWrongOften = findViewById(R.id.chipWrongOften);
    }

    private void setupGreeting() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Redirect to Login if no user
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        String name = "bạn";
        if (!user.isAnonymous()) {
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                name = user.getDisplayName();
            } else if (user.getEmail() != null) {
                int atIndex = user.getEmail().indexOf("@");
                name = atIndex > 0 ? user.getEmail().substring(0, atIndex) : user.getEmail();
            }
        }
        greetingTitle.setText("Xin chào, " + name + "!");
        greetingSubtitle.setText("Hôm nay bạn muốn\nôn phần nào?");
        
        // Update drawer header
        if (navigationView != null) {
            View headerView = navigationView.getHeaderView(0);
            if (headerView != null) {
                TextView navName = headerView.findViewById(R.id.nav_header_name);
                if (navName != null) {
                    navName.setText(name);
                }
            }
        }
    }

    private void setupChips() {
        // Set default selection
        chipPractice.setChecked(true);
        updateChipStyle(chipPractice, true);
        
        chipGroupCategories.setOnCheckedChangeListener((group, checkedId) -> {
            // Update all chip styles
            updateChipStyle(chipPractice, chipPractice.isChecked());
            updateChipStyle(chipMockExam, chipMockExam.isChecked());
            updateChipStyle(chipAI, chipAI.isChecked());
            updateChipStyle(chipCritical, chipCritical.isChecked());
            updateChipStyle(chipWrongOften, chipWrongOften.isChecked());
            
            // Handle navigation
            if (checkedId == chipPractice.getId()) {
                navigateToTopic();
            } else if (checkedId == chipMockExam.getId()) {
                navigateToLevel();
            } else if (checkedId == chipAI.getId()) {
                startActivity(new Intent(this, SmartPracticeActivity.class));
            }
        });
    }
    
    private void updateChipStyle(Chip chip, boolean isSelected) {
        if (isSelected) {
            chip.setChipBackgroundColorResource(R.color.color_primary);
            chip.setTextColor(getColor(R.color.color_on_primary));
        } else {
            chip.setChipBackgroundColorResource(R.color.color_surface_soft);
            chip.setTextColor(getColor(R.color.color_text_primary));
        }
    }

    private void setupCards() {
        cardBookmarks.setOnClickListener(v -> {
            startActivity(new Intent(this, BookmarksActivity.class));
        });
        
        cardHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
        });
        
        cardLeaderboard.setOnClickListener(v -> {
            startActivity(new Intent(this, LeaderboardActivity.class));
        });
        
        cardSmart.setOnClickListener(v -> {
            startActivity(new Intent(this, SmartPracticeActivity.class));
        });
    }

    private void setupCharts() {
        // Initialize charts with empty state
        ChartHelper.setupEmptyLineChart(lineChartScores);
        ChartHelper.setupCorrectIncorrectPieChart(pieChartCorrect, 0, 0);
    }

    /**
     * Load statistics for logged-in users from Firestore.
     */
    private void loadStatsIfLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.isAnonymous()) {
            statsHint.setText("Vui lòng đăng nhập để xem thống kê chi tiết về kết quả học của bạn.");
            return;
        }
        final String uid = user.getUid();
        statsHint.setText("Đang tải thống kê...");

        loadExamStats(uid);
    }

    private void loadExamStats(final String uid) {
        db.collection("exam_sessions")
                .whereEqualTo("user_id", uid)
                .get()
                .addOnSuccessListener(snap -> {
                    ExamSummary examSummary = computeExamSummary(snap);
                    loadAttemptStats(uid, examSummary);
                })
                .addOnFailureListener(e -> {
                    statsHint.setText("Không thể tải thống kê kỳ thi. Vui lòng kiểm tra kết nối mạng.");
                });
    }

    private ExamSummary computeExamSummary(QuerySnapshot snap) {
        ExamSummary summary = new ExamSummary();
        for (QueryDocumentSnapshot doc : snap) {
            Long scoreRaw = doc.getLong("score_raw");
            if (scoreRaw == null) {
                scoreRaw = doc.getLong("score");
            }
            Long submittedAt = doc.getLong("submitted_at_ms");
            if (submittedAt == null) {
                submittedAt = doc.getLong("submitted_at");
            }
            Long numCriticalWrong = doc.getLong("num_critical_wrong");
            if (numCriticalWrong == null) {
                numCriticalWrong = doc.getLong("num_liet_wrong");
            }
            if (scoreRaw == null) scoreRaw = 0L;
            if (submittedAt == null) submittedAt = 0L;

            summary.count++;
            summary.totalScore += scoreRaw;
            if (scoreRaw > summary.bestScore) {
                summary.bestScore = scoreRaw;
            }
            if (submittedAt > summary.lastSubmittedAt) {
                summary.lastSubmittedAt = submittedAt;
                summary.lastScore = scoreRaw;
                summary.lastCriticalWrong = numCriticalWrong != null ? numCriticalWrong : 0L;
            }
        }
        return summary;
    }

    private void loadAttemptStats(final String uid, final ExamSummary examSummary) {
        // Only look at attempts in last 30 days for topic performance & streak
        long now = System.currentTimeMillis();
        long thirtyDaysMs = 30L * 24L * 60L * 60L * 1000L;
        long fromTs = now - thirtyDaysMs;

        db.collection("attempts")
                .whereEqualTo("user_id", uid)
                .whereGreaterThanOrEqualTo("timestamp_ms", fromTs)
                .get()
                .addOnSuccessListener(snap -> {
                    AttemptSummary attemptSummary = computeAttemptSummary(snap);
                    updateStatsUI(examSummary, attemptSummary);
                })
                .addOnFailureListener(e -> {
                    updateStatsUI(examSummary, null);
                });
    }

    private AttemptSummary computeAttemptSummary(QuerySnapshot snap) {
        AttemptSummary summary = new AttemptSummary();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (QueryDocumentSnapshot doc : snap) {
            Boolean isCorrect = doc.getBoolean("is_correct");
            Long topicIdLong = doc.getLong("topic_id");
            if (topicIdLong == null) {
                topicIdLong = doc.getLong("category_id");
            }
            Long ts = doc.getLong("timestamp_ms");
            if (ts == null) {
                ts = doc.getLong("timestamp");
            }

            int topicId = topicIdLong != null ? topicIdLong.intValue() : 0;
            boolean correct = isCorrect != null && isCorrect;
            summary.totalAttempts++;
            if (correct) summary.totalCorrect++;

            // Topic performance
            TopicPerf perf = summary.topicPerfMap.get(topicId);
            if (perf == null) {
                perf = new TopicPerf();
                summary.topicPerfMap.put(topicId, perf);
            }
            perf.total++;
            if (correct) perf.correct++;

            // Streak: collect active days
            if (ts != null && ts > 0) {
                String dayKey = sdf.format(new Date(ts));
                summary.activeDays.add(dayKey);
            }
        }

        summary.longestStreak = computeLongestStreak(summary.activeDays);
        return summary;
    }

    private int computeLongestStreak(Set<String> dayKeys) {
        if (dayKeys.isEmpty()) return 0;
        Set<Long> days = new HashSet<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (String d : dayKeys) {
            try {
                long dayMs = sdf.parse(d).getTime() / (24L * 60L * 60L * 1000L);
                days.add(dayMs);
            } catch (Exception ignored) {
            }
        }
        int longest = 0;
        for (Long day : days) {
            if (!days.contains(day - 1)) {
                int len = 1;
                long cur = day;
                while (days.contains(cur + 1)) {
                    len++;
                    cur++;
                }
                if (len > longest) longest = len;
            }
        }
        return longest;
    }

    private void updateStatsUI(ExamSummary exam, AttemptSummary attempts) {
        // Update stat numbers
        if (exam != null && exam.count > 0) {
            txtStatExamCount.setText(String.valueOf(exam.count));
            txtStatBestScore.setText(String.valueOf(exam.bestScore));
            double avg = (double) exam.totalScore / exam.count;
            txtStatAvgScore.setText(String.format(Locale.getDefault(), "%.1f", avg));
        } else {
            txtStatExamCount.setText("0");
            txtStatBestScore.setText("0");
            txtStatAvgScore.setText("0");
        }

        // Update stats text
        StringBuilder sb = new StringBuilder();
        if (exam != null && exam.count > 0) {
            double avg = exam.count == 0 ? 0.0 : (double) exam.totalScore / exam.count;
            if (exam.lastSubmittedAt > 0) {
                Date lastDate = new Date(exam.lastSubmittedAt);
                SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                sb.append("Lần gần nhất: ").append(fmt.format(lastDate))
                        .append(" (điểm ").append(exam.lastScore);
                if (exam.lastCriticalWrong > 0) {
                    sb.append(", có ").append(exam.lastCriticalWrong).append(" câu điểm liệt sai");
                }
                sb.append(")");
            }
        } else {
            sb.append("Bạn chưa có lần thi thử nào. Hãy bắt đầu một bài thi để xem thống kê!");
        }

        if (attempts != null && attempts.totalAttempts > 0) {
            if (sb.length() > 0) sb.append("\n\n");
            sb.append("Hiệu suất luyện tập 30 ngày gần đây:\n");
            double acc = (double) attempts.totalCorrect * 100.0 / attempts.totalAttempts;
            sb.append("Tỉ lệ đúng: ").append(String.format(Locale.getDefault(), "%.1f", acc)).append("%");
        }

        statsHint.setText(sb.toString());

        // Update charts
        updateCharts(exam, attempts);
    }

    private void updateCharts(ExamSummary exam, AttemptSummary attempts) {
        // Line chart: Score over time (last 7-14 days)
        if (exam != null && exam.count > 0) {
            // For now, create placeholder data - in real implementation, 
            // you'd need to query exam_sessions with date grouping
            ArrayList<Float> scores = new ArrayList<>();
            ArrayList<String> labels = new ArrayList<>();
            
            // Placeholder: if we have exam data, show some sample trend
            if (exam.count >= 2) {
                scores.add((float) (exam.bestScore * 0.8));
                scores.add((float) exam.bestScore);
                labels.add("Tuần trước");
                labels.add("Tuần này");
            }
            
            if (!scores.isEmpty()) {
                ChartHelper.setupScoreLineChart(lineChartScores, scores, labels);
            } else {
                ChartHelper.setupEmptyLineChart(lineChartScores);
            }
        } else {
            ChartHelper.setupEmptyLineChart(lineChartScores);
        }

        // Pie chart: Correct/Incorrect ratio
        if (attempts != null && attempts.totalAttempts > 0) {
            int correct = attempts.totalCorrect;
            int incorrect = attempts.totalAttempts - attempts.totalCorrect;
            ChartHelper.setupCorrectIncorrectPieChart(pieChartCorrect, correct, incorrect);
        } else {
            ChartHelper.setupCorrectIncorrectPieChart(pieChartCorrect, 0, 0);
        }
    }

    // Helper structures for in-memory aggregation
    private static class ExamSummary {
        int count = 0;
        long totalScore = 0;
        long bestScore = 0;
        long lastSubmittedAt = 0;
        long lastScore = 0;
        long lastCriticalWrong = 0;
    }

    private static class TopicPerf {
        int total = 0;
        int correct = 0;
    }

    private static class AttemptSummary {
        int totalAttempts = 0;
        int totalCorrect = 0;
        Map<Integer, TopicPerf> topicPerfMap = new HashMap<>();
        Set<String> activeDays = new HashSet<>();
        int longestStreak = 0;
    }
}
