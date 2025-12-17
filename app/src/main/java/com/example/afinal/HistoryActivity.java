package com.example.afinal;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Hiển thị lịch sử làm bài (exam_sessions) của người dùng đã đăng nhập.
 */
public class HistoryActivity extends AppCompatActivity {

    private ListView listView;
    private Button backButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_history), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listView = findViewById(R.id.lvHistory);
        backButton = findViewById(R.id.btnHistoryBack);
        db = FirebaseFirestore.getInstance();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.isAnonymous()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử làm bài", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadHistory(user.getUid());
    }

    private void loadHistory(String uid) {
        // Để tránh lỗi index Firestore (where + orderBy), ta chỉ where theo user_id
        // rồi sort theo submitted_at_ms ở phía client.
        db.collection("exam_sessions")
                .whereEqualTo("user_id", uid)
                .get()
                .addOnSuccessListener(this::bindHistory)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Không thể tải lịch sử làm bài. Vui lòng kiểm tra kết nối mạng.", Toast.LENGTH_LONG).show();
                });
    }

    private void bindHistory(QuerySnapshot snap) {
        List<HistoryItem> items = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        for (QueryDocumentSnapshot doc : snap) {
            Long submittedAt = doc.getLong("submitted_at_ms");
            if (submittedAt == null) {
                // Backward compatibility: old field name
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
            Long durationMsLong = doc.getLong("duration_ms");

            int score = scoreRaw != null ? scoreRaw.intValue() : 0;
            int criticalWrong = numCriticalWrong != null ? numCriticalWrong.intValue() : 0;
            int minRequired = minRequiredLong != null ? minRequiredLong.intValue() : -1;
            int totalQuestions = totalQuestionsLong != null ? totalQuestionsLong.intValue() : 0;

            // Tên bài thi / hạng
            String levelName = doc.getString("level_name");
            Long levelId = doc.getLong("level_id");
            String examLabel;
            if (levelName != null && !levelName.isEmpty()) {
                examLabel = "Hạng " + levelName;
            } else if (levelId != null) {
                examLabel = "Hạng " + levelId;
            } else {
                examLabel = "Bài thi";
            }

            // Trạng thái: nếu có min_required thì áp dụng đúng rule đỗ/trượt như màn chọn hạng
            String status;
            if (minRequired > 0) {
                if (criticalWrong > 0 || score < minRequired) {
                    status = "Trượt";
                } else {
                    status = "Đỗ";
                }
            } else {
                // Backward compatibility: giữ rule cũ nếu chưa có min_required
                status = criticalWrong > 0 ? "Trượt" : "Đỗ";
            }

            long submittedVal = (submittedAt != null ? submittedAt : 0L);
            String timeStr = submittedVal > 0
                    ? fmt.format(new Date(submittedVal))
                    : "(không rõ thời gian)";

            // Thời gian làm bài thực tế (từ duration_ms)
            int timeMinutes = 0;
            int timeSeconds = 0;
            if (durationMsLong != null && durationMsLong > 0) {
                long totalSec = durationMsLong / 1000;
                timeMinutes = (int) (totalSec / 60);
                timeSeconds = (int) (totalSec % 60);
            }

            StringBuilder rowBuilder = new StringBuilder();
            rowBuilder.append(timeStr);
            rowBuilder.append(" • ").append(examLabel);
            if (totalQuestions > 0) {
                rowBuilder.append(" • Điểm: ").append(score).append("/").append(totalQuestions);
            } else {
                rowBuilder.append(" • Điểm: ").append(score);
            }
            rowBuilder.append(" • Trạng thái: ").append(status);
            if (timeMinutes > 0 || timeSeconds > 0) {
                rowBuilder.append(" • Thời gian: ");
                if (timeMinutes > 0) {
                    rowBuilder.append(timeMinutes).append(" phút");
                    if (timeSeconds > 0) rowBuilder.append(" ");
                }
                if (timeSeconds > 0) {
                    rowBuilder.append(timeSeconds).append(" giây");
                }
            }
            if (criticalWrong > 0) {
                rowBuilder.append(" • Sai ").append(criticalWrong).append(" câu điểm liệt");
            }

            HistoryItem item = new HistoryItem();
            item.submittedAt = submittedVal;
            item.text = rowBuilder.toString();
            items.add(item);
        }

        // Sort mới nhất lên đầu và giới hạn ~100 bản ghi cho gọn
        java.util.Collections.sort(items, (a, b) -> Long.compare(b.submittedAt, a.submittedAt));
        if (items.size() > 100) {
            items = items.subList(0, 100);
        }

        List<String> rows = new ArrayList<>();
        for (HistoryItem it : items) {
            rows.add(it.text);
        }

        if (rows.isEmpty()) {
            rows.add("Bạn chưa có lần thi thử nào.");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                rows
        );
        listView.setAdapter(adapter);
    }

    private static class HistoryItem {
        long submittedAt;
        String text;
    }
}


