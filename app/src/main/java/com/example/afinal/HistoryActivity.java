package com.example.afinal;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.afinal.adapter.HistoryAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Hiển thị lịch sử làm bài (exam_sessions) của người dùng đã đăng nhập.
 */
public class HistoryActivity extends BaseNavigationActivity {

    private RecyclerView rvHistory;
    private TextView txtEmptyState;
    private FirebaseFirestore db;
    private HistoryAdapter adapter;
    private List<HistoryAdapter.HistoryItem> historyItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_new);

        // Setup navigation
        setupToolbar(false, "Lịch sử làm bài");
        enableBottomBar(3); // More tab

        rvHistory = findViewById(R.id.rv_history);
        txtEmptyState = findViewById(R.id.txt_empty_state);
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.isAnonymous()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử làm bài", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        historyItems = new ArrayList<>();
        adapter = new HistoryAdapter(this, historyItems);
        rvHistory.setAdapter(adapter);

        loadHistory(user.getUid());
    }

    private void loadHistory(String uid) {
        db.collection("exam_sessions")
                .whereEqualTo("user_id", uid)
                .get()
                .addOnSuccessListener(this::bindHistory)
                .addOnFailureListener(e -> {
                    txtEmptyState.setVisibility(View.VISIBLE);
                    txtEmptyState.setText("Không thể tải lịch sử làm bài. Vui lòng kiểm tra kết nối mạng.");
                    rvHistory.setVisibility(View.GONE);
                });
    }

    private void bindHistory(QuerySnapshot snap) {
        historyItems.clear();

        for (QueryDocumentSnapshot doc : snap) {
            Long submittedAt = doc.getLong("submitted_at_ms");
            if (submittedAt == null) {
                submittedAt = doc.getLong("submitted_at");
            }
            Long scoreRaw = doc.getLong("score_raw");
            if (scoreRaw == null) {
                scoreRaw = doc.getLong("score");
            }
            Long numCriticalWrong = doc.getLong("num_critical_wrong");
            if (numCriticalWrong == null) {
                numCriticalWrong = doc.getLong("num_liet_wrong");
            }
            Long minRequiredLong = doc.getLong("min_required");
            Long totalQuestionsLong = doc.getLong("total_questions");

            int score = scoreRaw != null ? scoreRaw.intValue() : 0;
            int criticalWrong = numCriticalWrong != null ? numCriticalWrong.intValue() : 0;
            int minRequired = minRequiredLong != null ? minRequiredLong.intValue() : -1;
            int totalQuestions = totalQuestionsLong != null ? totalQuestionsLong.intValue() : 0;

            // Tên bài thi / hạng - ưu tiên level_name từ Firestore
            String levelName = doc.getString("level_name");
            Long levelId = doc.getLong("level_id");
            String examLabel;
            if (levelName != null && !levelName.isEmpty()) {
                examLabel = "Hạng " + levelName;  // Hiển thị "Hạng B", "Hạng C1", etc.
            } else if (levelId != null) {
                examLabel = "Hạng " + levelId;
            } else {
                examLabel = "Bài thi";  // Fallback nếu không có level info
            }

            // Trạng thái: ưu tiên dùng field "status" từ Firestore, nếu không có thì tính lại
            boolean passed;
            String statusFromDb = doc.getString("status");
            if (statusFromDb != null) {
                // Dùng status từ database: "Đỗ" = true, "Trượt" = false
                passed = "Đỗ".equals(statusFromDb);
            } else {
                // Fallback: tính lại từ logic cũ (backward compatibility)
                if (minRequired > 0) {
                    passed = criticalWrong == 0 && score >= minRequired;
                } else {
                    passed = criticalWrong == 0;
                }
            }

            long submittedVal = (submittedAt != null ? submittedAt : 0L);

            historyItems.add(new HistoryAdapter.HistoryItem(
                examLabel, 
                submittedVal, 
                score, 
                totalQuestions,
                passed
            ));
        }

        // Sort by time descending
        Collections.sort(historyItems, new Comparator<HistoryAdapter.HistoryItem>() {
            @Override
            public int compare(HistoryAdapter.HistoryItem a, HistoryAdapter.HistoryItem b) {
                return Long.compare(b.submittedAt, a.submittedAt);
            }
        });

        adapter.notifyDataSetChanged();

        // Show/hide empty state
        if (historyItems.isEmpty()) {
            txtEmptyState.setVisibility(View.VISIBLE);
            txtEmptyState.setText("Chưa có lịch sử làm bài");
            rvHistory.setVisibility(View.GONE);
        } else {
            txtEmptyState.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
        }
    }
}
