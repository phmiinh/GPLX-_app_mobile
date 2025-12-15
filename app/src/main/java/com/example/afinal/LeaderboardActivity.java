package com.example.afinal;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LeaderboardActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView title;
    private TextView placeholder;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_leaderboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_leaderboard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnBack = findViewById(R.id.btnLeaderboardBack);
        title = findViewById(R.id.txtLeaderboardTitle);
        placeholder = findViewById(R.id.txtLeaderboardPlaceholder);

        db = FirebaseFirestore.getInstance();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        placeholder.setText("Đang tải bảng xếp hạng...");
        db.collection("exam_sessions")
                .whereGreaterThan("score_raw", 0)
                .get()
                .addOnSuccessListener(this::handleExamSessions)
                .addOnFailureListener(e -> placeholder.setText("Không thể tải bảng xếp hạng. Vui lòng kiểm tra kết nối mạng."));
    }

    private static class UserStats {
        long bestScore = 0;
        long totalScore = 0;
        int examCount = 0;
    }

    private static class LeaderboardRow {
        String userId;
        String displayName;
        long bestScore;
        double avgScore;
        int examCount;
    }

    private void handleExamSessions(QuerySnapshot snap) {
        // Aggregate per user
        Map<String, UserStats> statsMap = new HashMap<>();
        for (QueryDocumentSnapshot doc : snap) {
            String userId = doc.getString("user_id");
            if (userId == null || userId.isEmpty()) continue;
            Long scoreRaw = doc.getLong("score_raw");
            if (scoreRaw == null) scoreRaw = 0L;

            UserStats st = statsMap.get(userId);
            if (st == null) {
                st = new UserStats();
                statsMap.put(userId, st);
            }
            st.examCount++;
            st.totalScore += scoreRaw;
            if (scoreRaw > st.bestScore) st.bestScore = scoreRaw;
        }

        if (statsMap.isEmpty()) {
            placeholder.setText("Chưa có dữ liệu thi thử để tạo bảng xếp hạng.");
            return;
        }

        // Load user profiles to get display_name / email
        db.collection("users")
                .get()
                .addOnSuccessListener(userSnap -> buildLeaderboard(statsMap, userSnap))
                .addOnFailureListener(e -> buildLeaderboard(statsMap, null));
    }

    private void buildLeaderboard(Map<String, UserStats> statsMap, QuerySnapshot userSnap) {
        Map<String, String> nameMap = new HashMap<>();
        if (userSnap != null) {
            for (QueryDocumentSnapshot doc : userSnap) {
                String uid = doc.getId();
                String displayName = doc.getString("display_name");
                String email = doc.getString("email");
                if (displayName == null || displayName.isEmpty()) {
                    if (email != null) {
                        int at = email.indexOf("@");
                        displayName = at > 0 ? email.substring(0, at) : email;
                    } else {
                        displayName = uid.substring(0, Math.min(6, uid.length()));
                    }
                }
                nameMap.put(uid, displayName);
            }
        }

        List<LeaderboardRow> rows = new ArrayList<>();
        for (Map.Entry<String, UserStats> e : statsMap.entrySet()) {
            String uid = e.getKey();
            UserStats st = e.getValue();
            if (st.examCount == 0) continue;
            LeaderboardRow row = new LeaderboardRow();
            row.userId = uid;
            row.displayName = nameMap.containsKey(uid) ? nameMap.get(uid) : uid.substring(0, Math.min(6, uid.length()));
            row.bestScore = st.bestScore;
            row.avgScore = (double) st.totalScore / st.examCount;
            row.examCount = st.examCount;
            rows.add(row);
        }

        if (rows.isEmpty()) {
            placeholder.setText("Chưa có dữ liệu thi thử để tạo bảng xếp hạng.");
            return;
        }

        // Sort for best-score leaderboard
        List<LeaderboardRow> bestList = new ArrayList<>(rows);
        Collections.sort(bestList, new Comparator<LeaderboardRow>() {
            @Override
            public int compare(LeaderboardRow o1, LeaderboardRow o2) {
                return Long.compare(o2.bestScore, o1.bestScore);
            }
        });

        // Sort for average-score leaderboard
        List<LeaderboardRow> avgList = new ArrayList<>(rows);
        Collections.sort(avgList, new Comparator<LeaderboardRow>() {
            @Override
            public int compare(LeaderboardRow o1, LeaderboardRow o2) {
                return Double.compare(o2.avgScore, o1.avgScore);
            }
        });

        StringBuilder sb = new StringBuilder();
        sb.append("TOP theo điểm cao nhất:\n");
        appendTable(sb, bestList);
        sb.append("\nTOP theo điểm trung bình:\n");
        appendTable(sb, avgList);

        placeholder.setText(sb.toString());
    }

    private void appendTable(StringBuilder sb, List<LeaderboardRow> list) {
        int limit = Math.min(10, list.size());
        for (int i = 0; i < limit; i++) {
            LeaderboardRow r = list.get(i);
            sb.append(String.format(Locale.getDefault(),
                    "%d. %s - Best: %d, Avg: %.1f (%d lần thi)\n",
                    i + 1, r.displayName, r.bestScore, r.avgScore, r.examCount));
        }
    }
}


