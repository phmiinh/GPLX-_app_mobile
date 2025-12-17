package com.example.afinal;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.afinal.adapter.BookmarkAdapter;
import com.example.afinal.analytics.AnalyticsRepository;
import com.example.afinal.dbclass.Question;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class BookmarksActivity extends BaseNavigationActivity {
    private static final String TAG = "BookmarksActivity";
    private RecyclerView rvBookmarks;
    private LinearLayout emptyState;
    private SQLiteDatabase database = null;
    private AnalyticsRepository analyticsRepository;
    private BookmarkAdapter adapter;
    private ArrayList<Question> questions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        // Setup navigation
        setupToolbar(false, "Câu hỏi đã đánh dấu");
        enableBottomBar(3); // More tab

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null || firebaseUser.isAnonymous()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem danh sách câu hỏi đã đánh dấu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            analyticsRepository = new AnalyticsRepository(this);
            analyticsRepository.ensureSchema();

            database = openOrCreateDatabase("ATGT.db", MODE_PRIVATE, null);
            if (database == null) {
                Log.e(TAG, "Failed to open database");
                Toast.makeText(this, "Không thể mở cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Initialize views
            rvBookmarks = findViewById(R.id.rv_bookmarks);
            emptyState = findViewById(R.id.empty_state);

            questions = new ArrayList<>();
            adapter = new BookmarkAdapter(this, questions, new BookmarkAdapter.OnBookmarkClickListener() {
                @Override
                public void onBookmarkClick(Question question, int position) {
                    if (question == null) {
                        Log.e(TAG, "Question is null when clicking bookmark");
                        Toast.makeText(BookmarksActivity.this, "Lỗi: Không tìm thấy câu hỏi", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Navigate to question detail
                    Log.d(TAG, "Navigating to QuestionDetailActivity with question: " + question.getContent());
                    Intent intent = new Intent(BookmarksActivity.this, QuestionDetailActivity.class);
                    intent.putExtra("question", question);
                    startActivity(intent);
                }
            });
            rvBookmarks.setAdapter(adapter);

            // Load bookmarks from Firestore first; fallback to SQLite
            String userId = firebaseUser.getUid();
            loadBookmarksFromFirestore(userId);

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Load bookmarks from Firestore `bookmarks` collection for current user.
     * For each `question_id`, we join with local SQLite `questions` table to build full Question objects.
     * If Firestore query fails, we fall back to local bookmarks table.
     */
    private void loadBookmarksFromFirestore(final String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "User ID is null or empty");
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("bookmarks")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        try {
                            Set<String> idSet = new HashSet<>();
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                String qid = doc.getString("question_id");
                                if (qid != null && !qid.isEmpty()) {
                                    idSet.add(qid);
                                }
                            }
                            if (idSet.isEmpty()) {
                                Log.d(TAG, "No Firestore bookmarks, falling back to local");
                                loadBookmarkedQuestionsLocal(userId);
                                return;
                            }
                            loadQuestionsByIds(new ArrayList<>(idSet));
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing Firestore bookmarks", e);
                            loadBookmarkedQuestionsLocal(userId);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to load bookmarks from Firestore", e);
                        loadBookmarkedQuestionsLocal(userId);
                    }
                });
    }

    /**
     * Fallback: load bookmarked question IDs from local SQLite bookmarks table via AnalyticsRepository.
     */
    private void loadBookmarkedQuestionsLocal(String userId) {
        try {
            List<String> ids = analyticsRepository.getBookmarkedQuestionIds(userId);
            if (ids == null || ids.isEmpty()) {
                Log.d(TAG, "No local bookmarks found for user: " + userId);
                Toast.makeText(this, "Chưa có câu hỏi nào được đánh dấu", Toast.LENGTH_SHORT).show();
                return;
            }
            loadQuestionsByIds(ids);
        } catch (Exception e) {
            Log.e(TAG, "Error in loadBookmarkedQuestionsLocal", e);
            Toast.makeText(this, "Lỗi khi tải danh sách câu hỏi đã đánh dấu", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Given a list of question IDs (as string), query local `questions` table and build Question objects.
     */
    private void loadQuestionsByIds(List<String> ids) {
        if (database == null || !database.isOpen()) {
            database = openOrCreateDatabase("ATGT.db", MODE_PRIVATE, null);
        }
        if (database == null) {
            Log.e(TAG, "Database is null when loading questions");
            return;
        }
        questions.clear();
        for (String id : ids) {
            if (id == null || id.isEmpty()) continue;
            Cursor cursor = null;
            try {
                cursor = database.query("questions", null, "question_id=?", new String[]{id}, null, null, null);
                if (cursor == null || !cursor.moveToFirst()) {
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
                questions.add(q);
            } catch (Exception e) {
                Log.e(TAG, "Error loading question with id: " + id, e);
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        adapter.notifyDataSetChanged();
        
        // Show/hide empty state
        if (questions.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvBookmarks.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvBookmarks.setVisibility(View.VISIBLE);
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
