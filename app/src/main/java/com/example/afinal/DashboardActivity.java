package com.example.afinal;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

    /**
     * Màn hình Home/Dashboard chính:
     * - Chào user đang đăng nhập
     * - Cung cấp nút truy cập Luyện tập / Thi thử / Ôn thông minh (AI) / Leaderboard
     * - Hiển thị thống kê kết quả học của user.
     */
public class DashboardActivity extends AppCompatActivity {

    private TextView greetingTitle;
    private TextView greetingSubtitle;
    private TextView statsHint;
    private ImageButton btnMenu;
    private Button btnPractice;
    private Button btnMockExam;
    private Button btnSmart;
    private Button btnLeaderboard;
    private Button btnBookmarks;
    private Button btnHistory;

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
        db = FirebaseFirestore.getInstance();
        setupGreeting();
        setupButtons();
        loadStatsIfLoggedIn();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Mỗi lần quay lại Dashboard, tải lại thống kê để luôn “realtime”
        loadStatsIfLoggedIn();
    }

    private void bindViews() {
        greetingTitle = findViewById(R.id.txtDashboardTitle);
        greetingSubtitle = findViewById(R.id.txtDashboardSubtitle);
        statsHint = findViewById(R.id.txtDashboardStatsHint);
        btnMenu = findViewById(R.id.btnMenuMain);
        btnPractice = findViewById(R.id.btnDashboardPractice);
        btnMockExam = findViewById(R.id.btnDashboardMockExam);
        btnSmart = findViewById(R.id.btnDashboardSmart);
        btnLeaderboard = findViewById(R.id.btnDashboardLeaderboard);
        btnBookmarks = findViewById(R.id.btnDashboardBookmarks);
        btnHistory = findViewById(R.id.btnDashboardHistory);

        if (btnMenu != null) {
            btnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupMenu();
                }
            });
        }
    }

    private void setupGreeting() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // If somehow no user, redirect to Login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        String name = "bạn";
        if (user != null && !user.isAnonymous()) {
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                name = user.getDisplayName();
            } else if (user.getEmail() != null) {
                // Shorten email for greeting
                int atIndex = user.getEmail().indexOf("@");
                name = atIndex > 0 ? user.getEmail().substring(0, atIndex) : user.getEmail();
            }
        }
        greetingTitle.setText("Xin chào " + name);
        greetingSubtitle.setText("Hãy trải nghiệm ngay nào!");
    }

    private void setupButtons() {
        btnPractice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ôn tập theo chủ đề
                Intent intent = new Intent(DashboardActivity.this, TopicActivity.class);
                startActivity(intent);
            }
        });
        btnMockExam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Thi thử theo hạng
                Intent intent = new Intent(DashboardActivity.this, LevelActivity.class);
                startActivity(intent);
            }
        });
        btnSmart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ôn thông minh (AI)
                Intent intent = new Intent(DashboardActivity.this, SmartPracticeActivity.class);
                startActivity(intent);
            }
        });
        btnLeaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, LeaderboardActivity.class);
                startActivity(intent);
            }
        });
        if (btnBookmarks != null) {
            btnBookmarks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(DashboardActivity.this, BookmarksActivity.class));
                }
            });
        }
        if (btnHistory != null) {
            btnHistory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(DashboardActivity.this, HistoryActivity.class));
                }
            });
        }
    }

    private void showPopupMenu() {
        LayoutInflater inflater = getLayoutInflater();
        View popup = inflater.inflate(R.layout.menu_main, null);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        final PopupWindow popupWindow = new PopupWindow(
                popup,
                (int) (screenWidth * 0.7),
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
        popupWindow.setElevation(10f);
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.TOP | Gravity.START, 0, 0);

        TextView level = popup.findViewById(R.id.txtMenuLevel);
        level.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, LevelActivity.class));
                popupWindow.dismiss();
            }
        });

        TextView topic = popup.findViewById(R.id.txtMenuTopic);
        topic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, TopicActivity.class));
                popupWindow.dismiss();
            }
        });

        TextView bookmarked = popup.findViewById(R.id.textView7);
        bookmarked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, BookmarksActivity.class));
                popupWindow.dismiss();
            }
        });

        TextView history = popup.findViewById(R.id.textView9);
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, HistoryActivity.class));
                popupWindow.dismiss();
            }
        });
    }

    /**
     * Load statistics for logged-in users from Firestore.
     * Anonymous users only see a prompt to log in.
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
                // Backward-compatible field name
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
                    updateStatsText(examSummary, attemptSummary);
                })
                .addOnFailureListener(e -> {
                    // Even if attempts fail, still show exam stats
                    updateStatsText(examSummary, null);
                });
    }

    private AttemptSummary computeAttemptSummary(QuerySnapshot snap) {
        AttemptSummary summary = new AttemptSummary();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (QueryDocumentSnapshot doc : snap) {
            Boolean isCorrect = doc.getBoolean("is_correct");
            Long topicIdLong = doc.getLong("topic_id");
            if (topicIdLong == null) {
                // Backward-compatible: some logs may use category_id
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

        // Compute simple streak: longest run of consecutive days in activeDays
        summary.longestStreak = computeLongestStreak(summary.activeDays);
        return summary;
    }

    private int computeLongestStreak(Set<String> dayKeys) {
        if (dayKeys.isEmpty()) return 0;
        // Convert to millis for easy arithmetic
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

    private void updateStatsText(ExamSummary exam, AttemptSummary attempts) {
        StringBuilder sb = new StringBuilder();
        if (exam != null && exam.count > 0) {
            double avg = exam.count == 0 ? 0.0 : (double) exam.totalScore / exam.count;
            sb.append("Thi thử: ")
                    .append(exam.count).append(" lần\n")
                    .append("Điểm cao nhất: ").append(exam.bestScore).append("\n")
                    .append("Điểm trung bình: ").append(String.format(Locale.getDefault(), "%.1f", avg)).append("\n");
            if (exam.lastSubmittedAt > 0) {
                Date lastDate = new Date(exam.lastSubmittedAt);
                SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                sb.append("Lần gần nhất: ").append(fmt.format(lastDate))
                        .append(" (điểm ").append(exam.lastScore);
                if (exam.lastCriticalWrong > 0) {
                    sb.append(", có ").append(exam.lastCriticalWrong).append(" câu điểm liệt sai");
                }
                sb.append(")\n");
            }
        } else {
            sb.append("Bạn chưa có lần thi thử nào. Hãy bắt đầu một bài thi để xem thống kê!\n");
        }

        if (attempts != null && attempts.totalAttempts > 0) {
            sb.append("\nHiệu suất luyện tập 30 ngày gần đây:\n");
            double acc = (double) attempts.totalCorrect * 100.0 / attempts.totalAttempts;
            sb.append("- Tỉ lệ đúng tổng: ")
                    .append(String.format(Locale.getDefault(), "%.1f", acc)).append("%\n");

            // Show up to 3 topics summary
            int shown = 0;
            for (Map.Entry<Integer, TopicPerf> e : attempts.topicPerfMap.entrySet()) {
                if (shown >= 3) break;
                TopicPerf perf = e.getValue();
                if (perf.total == 0) continue;
                double topicAcc = (double) perf.correct * 100.0 / perf.total;
                sb.append(String.format(Locale.getDefault(),
                        "- Chủ đề %d: %.1f%% đúng (%d/%d)\n",
                        e.getKey(), topicAcc, perf.correct, perf.total));
                shown++;
            }

            sb.append("\nChuỗi ngày luyện tập dài nhất: ")
                    .append(attempts.longestStreak)
                    .append(" ngày liên tiếp\n");
            sb.append("Số ngày có luyện tập trong 30 ngày gần đây: ")
                    .append(attempts.activeDays.size())
                    .append(" ngày");
        }

        statsHint.setText(sb.toString());
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


