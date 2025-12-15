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
        db.collection("exam_sessions")
                .whereEqualTo("user_id", uid)
                .orderBy("submitted_at_ms", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(this::bindHistory)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Không thể tải lịch sử làm bài. Vui lòng kiểm tra kết nối mạng.", Toast.LENGTH_LONG).show();
                });
    }

    private void bindHistory(QuerySnapshot snap) {
        List<String> rows = new ArrayList<>();
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

            int score = scoreRaw != null ? scoreRaw.intValue() : 0;
            int criticalWrong = numCriticalWrong != null ? numCriticalWrong.intValue() : 0;
            String status = criticalWrong > 0 ? "Trượt" : "Đỗ";

            String timeStr = submittedAt != null && submittedAt > 0
                    ? fmt.format(new Date(submittedAt))
                    : "(không rõ thời gian)";

            String row = timeStr + " • Điểm: " + score + " • Trạng thái: " + status;
            if (criticalWrong > 0) {
                row += " • Sai " + criticalWrong + " câu điểm liệt";
            }
            rows.add(row);
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
}


