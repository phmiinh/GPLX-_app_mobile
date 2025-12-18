package com.example.afinal;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.afinal.adapter.LeaderboardAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
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

        // Lấy tất cả unique user IDs để query display_name
        List<String> userIds = new ArrayList<>(userStatsMap.keySet());
        
        if (userIds.isEmpty()) {
            txtPlaceholder.setText("Chưa có dữ liệu bảng xếp hạng");
            txtPlaceholder.setVisibility(View.VISIBLE);
            rvLeaderboard.setVisibility(View.GONE);
            return;
        }

        // Query users collection để lấy display_name
        // Firestore whereIn() có giới hạn 10 items, nên cần chia nhỏ nếu > 10 users
        loadUserNames(userIds, userStatsMap);
    }

    private void loadUserNames(List<String> userIds, Map<String, UserStats> userStatsMap) {
        Map<String, String> userIdToDisplayName = new HashMap<>();
        final int[] completedQueries = {0};
        final int totalQueries = userIds.size();

        if (totalQueries == 0) {
            buildLeaderboardRows(userStatsMap, userIdToDisplayName);
            return;
        }

        // Query từng user document để lấy display_name
        for (String userId : userIds) {
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String displayName = documentSnapshot.getString("display_name");
                            if (displayName != null && !displayName.isEmpty()) {
                                userIdToDisplayName.put(userId, displayName);
                            }
                        }
                        
                        completedQueries[0]++;
                        if (completedQueries[0] >= totalQueries) {
                            // Tất cả queries đã hoàn thành, tạo leaderboard rows
                            buildLeaderboardRows(userStatsMap, userIdToDisplayName);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Nếu query user thất bại, bỏ qua và tiếp tục
                        completedQueries[0]++;
                        if (completedQueries[0] >= totalQueries) {
                            buildLeaderboardRows(userStatsMap, userIdToDisplayName);
                        }
                    });
        }
    }

    private void buildLeaderboardRows(Map<String, UserStats> userStatsMap, Map<String, String> userIdToDisplayName) {
        leaderboardRows.clear();
        
        for (Map.Entry<String, UserStats> entry : userStatsMap.entrySet()) {
            String userId = entry.getKey();
            UserStats stats = entry.getValue();
            double avgScore = stats.examCount > 0 
                ? (double) stats.totalScore / stats.examCount 
                : 0.0;

            // Lấy display_name, nếu không có thì dùng userId rút ngắn
            String displayName = userIdToDisplayName.get(userId);
            if (displayName == null || displayName.isEmpty()) {
                // Rút ngắn userId để hiển thị (lấy 8 ký tự đầu + "...")
                displayName = userId.length() > 8 ? userId.substring(0, 8) + "..." : userId;
            }

            leaderboardRows.add(new LeaderboardAdapter.LeaderboardRow(
                0, // rank will be set after sorting
                displayName,
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
