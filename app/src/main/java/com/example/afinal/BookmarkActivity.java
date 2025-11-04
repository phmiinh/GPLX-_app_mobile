package com.example.afinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.afinal.analytics.FirestoreService;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.afinal.adapter.BookmarkListAdapter;
import com.example.afinal.analytics.UserIdentity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BookmarkActivity extends AppCompatActivity implements BookmarkListAdapter.OnItemClickListener {

    private RecyclerView rvBookmarks;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ImageButton back;
    private BookmarkListAdapter adapter;
    private FirestoreService firestoreService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        // Khởi tạo UI
        rvBookmarks = findViewById(R.id.rvBookmarks);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        back = findViewById(R.id.back);

        // Khởi tạo dịch vụ
        firestoreService = new FirestoreService();

        // Thiết lập RecyclerView
        adapter = new BookmarkListAdapter(this, new ArrayList<>(), this);
        rvBookmarks.setLayoutManager(new LinearLayoutManager(this));
        rvBookmarks.setAdapter(adapter);

        // Thiết lập nút Back
        back.setOnClickListener(v -> finish());

        // Tải dữ liệu Bookmark
        loadBookmarks();
    }

    /**
     * Tải danh sách ID câu hỏi đã đánh dấu từ Firestore.
     */
    private void loadBookmarks() {
        showLoading(true);
        String userId = UserIdentity.getUserId(this);

        // Sửa lỗi: Sử dụng BookmarkListListener
        firestoreService.getBookmarkList(userId, new FirestoreService.BookmarkListListener() {
            @Override
            public void onResult(List<Integer> questionIds) {
                showLoading(false);
                if (questionIds.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvBookmarks.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rvBookmarks.setVisibility(View.VISIBLE);
                    adapter.setQuestionIds(questionIds);
                }
            }

            @Override
            public void onError(Exception e) {
                showLoading(false);
                tvEmpty.setText("Lỗi khi tải dữ liệu. Vui lòng thử lại.");
                tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(BookmarkActivity.this, "Lỗi tải Bookmark: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        rvBookmarks.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
    }

    /**
     * Xử lý khi người dùng click vào một item câu hỏi.
     */
    @Override
    public void onItemClick(int questionId, int position) {
        List<Integer> questionIds = adapter.getQuestionIds();

        // Kiểm tra danh sách ID câu hỏi có hợp lệ không
        if (questionIds == null || questionIds.isEmpty()) {
            Toast.makeText(this, "Không có câu hỏi nào để xem.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển sang QuestionActivityBookmark để làm bài
        Intent intent = new Intent(this, QuestionActivityBookmark.class);

        // Truyền toàn bộ danh sách ID câu hỏi đã đánh dấu và vị trí bắt đầu
        intent.putExtra("list", (Serializable) questionIds);
        intent.putExtra("position", position);

        startActivity(intent);
    }
}
