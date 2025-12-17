package com.example.afinal;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.afinal.adapter.LeaderboardAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardActivity extends BaseNavigationActivity {

    private RecyclerView rvLeaderboard;
    private TextView txtPlaceholder;
    private FirebaseFirestore db;
    private LeaderboardAdapter adapter;
    private List<LeaderboardAdapter.LeaderboardRow> leaderboardRows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard_new);

        // Setup navigation
        setupToolbar(false, "Bảng xếp hạng");
        enableBottomBar(3); // More tab

        rvLeaderboard = findViewById(R.id.rv_leaderboard);
        txtPlaceholder = findViewById(R.id.txt_leaderboard_placeholder);
        db = FirebaseFirestore.getInstance();

        leaderboardRows = new ArrayList<>();
        adapter = new LeaderboardAdapter(this, leaderboardRows);
        rvLeaderboard.setAdapter(adapter);

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        txtPlaceholder.setText("Đang tải bảng xếp hạng...");
        txtPlaceholder.setVisibility(View.VISIBLE);
        rvLeaderboard.setVisibility(View.GONE);

        db.collection("exam_sessions")
                .whereGreaterThan("score_raw", 0)
                .get()
                .addOnSuccessListener(this::handleExamSessions)
                .addOnFailureListener(e -> {
                    txtPlaceholder.setText("Không thể tải bảng xếp hạng. Vui lòng kiểm tra kết nối mạng.");
                });
    }

    private static class UserStats {
        long bestScore = 0;
        long totalScore = 0;
        int examCount = 0;
    }

    private void handleExamSessions(QuerySnapshot snapshot) {
        Map<String, UserStats> userStatsMap = new HashMap<>();

        for (QueryDocumentSnapshot doc : snapshot) {
            String userId = doc.getString("user_id");
            Long scoreRaw = doc.getLong("score_raw");
            if (userId == null || scoreRaw == null) continue;

            UserStats stats = userStatsMap.get(userId);
            if (stats == null) {
                stats = new UserStats();
                userStatsMap.put(userId, stats);
            }

            stats.examCount++;
            stats.totalScore += scoreRaw;
            if (scoreRaw > stats.bestScore) {
                stats.bestScore = scoreRaw;
            }
        }

        leaderboardRows.clear();
        for (Map.Entry<String, UserStats> entry : userStatsMap.entrySet()) {
            String userId = entry.getKey();
            UserStats stats = entry.getValue();
            double avgScore = stats.examCount > 0 
                ? (double) stats.totalScore / stats.examCount 
                : 0.0;

            leaderboardRows.add(new LeaderboardAdapter.LeaderboardRow(
                0, // rank will be set after sorting
                userId,
                stats.bestScore,
                avgScore,
                stats.examCount
            ));
        }

        // Sort by best score descending
        Collections.sort(leaderboardRows, new Comparator<LeaderboardAdapter.LeaderboardRow>() {
            @Override
            public int compare(LeaderboardAdapter.LeaderboardRow a, LeaderboardAdapter.LeaderboardRow b) {
                return Long.compare(b.bestScore, a.bestScore);
            }
        });

        // Assign ranks
        for (int i = 0; i < leaderboardRows.size(); i++) {
            leaderboardRows.get(i).rank = i + 1;
        }

        adapter.notifyDataSetChanged();

        if (leaderboardRows.isEmpty()) {
            txtPlaceholder.setText("Chưa có dữ liệu bảng xếp hạng");
            txtPlaceholder.setVisibility(View.VISIBLE);
            rvLeaderboard.setVisibility(View.GONE);
        } else {
            txtPlaceholder.setVisibility(View.GONE);
            rvLeaderboard.setVisibility(View.VISIBLE);
        }
    }
}
