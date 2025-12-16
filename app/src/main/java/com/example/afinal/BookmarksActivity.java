package com.example.afinal;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.afinal.adapter.QuestionAdapter;
import com.example.afinal.analytics.AnalyticsRepository;
import com.example.afinal.analytics.UserIdentity;
import com.example.afinal.dbclass.Question;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BookmarksActivity extends AppCompatActivity {
    private static final String TAG = "BookmarksActivity";
    private ListView listView;
    private Button back;
    private SQLiteDatabase database = null;
    private AnalyticsRepository analyticsRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_question_review);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null || firebaseUser.isAnonymous()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem danh sách câu hỏi đã đánh dấu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            analyticsRepository = new AnalyticsRepository(this);
            // Ensure schema is initialized
            analyticsRepository.ensureSchema();
            
            database = openOrCreateDatabase("ATGT.db", MODE_PRIVATE, null);
            if (database == null) {
                Log.e(TAG, "Failed to open database");
                Toast.makeText(this, "Không thể mở cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            listView = findViewById(R.id.lvQAR);
            back = findViewById(R.id.btnQARback);

            if (listView == null) {
                Log.e(TAG, "ListView not found in layout");
                Toast.makeText(this, "Lỗi giao diện", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            if (back == null) {
                Log.e(TAG, "Back button not found in layout");
                Toast.makeText(this, "Lỗi giao diện", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            ArrayList<Question> questions = new ArrayList<>();
            loadBookmarkedQuestions(questions);

            QuestionAdapter adapter = new QuestionAdapter(this, R.layout.layout_listview_review, questions);
            listView.setAdapter(adapter);

            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadBookmarkedQuestions(ArrayList<Question> out) {
        try {
            String userId = UserIdentity.getUserId(this);
            if (userId == null || userId.isEmpty()) {
                Log.e(TAG, "User ID is null or empty");
                return;
            }

            List<String> ids = analyticsRepository.getBookmarkedQuestionIds(userId);
            if (ids == null || ids.isEmpty()) {
                Log.d(TAG, "No bookmarked questions found for user: " + userId);
                return;
            }

            if (database == null || !database.isOpen()) {
                Log.e(TAG, "Database is null or closed");
                database = openOrCreateDatabase("ATGT.db", MODE_PRIVATE, null);
                if (database == null) {
                    Log.e(TAG, "Failed to reopen database");
                    return;
                }
            }

            for (String id : ids) {
                if (id == null || id.isEmpty()) {
                    continue;
                }
                try {
                    Cursor cursor = database.query("questions", null, "question_id=?", new String[]{id}, null, null, null);
                    if (cursor == null) {
                        continue;
                    }
                    if (!cursor.moveToFirst()) {
                        cursor.close();
                        continue;
                    }
                    Question q = new Question();
                    q.setId(cursor.getInt(0));
                    q.setContent(cursor.getString(2));
                    q.setImg_url(cursor.getString(3));
                    q.setExplain(cursor.getString(5));
                    q.setA(cursor.getString(6));
                    q.setB(cursor.getString(7));
                    q.setC(cursor.getString(8));
                    q.setD(cursor.getString(9));
                    q.setAnswer(cursor.getString(10));
                    q.setIs_critical(cursor.getInt(4));
                    q.setUserChoice("");
                    out.add(q);
                    cursor.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error loading question with id: " + id, e);
                    // Continue with next question
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in loadBookmarkedQuestions", e);
            Toast.makeText(this, "Lỗi khi tải danh sách câu hỏi đã đánh dấu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null && database.isOpen()) {
            database.close();
        }
    }
}



