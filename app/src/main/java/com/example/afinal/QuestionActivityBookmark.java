package com.example.afinal;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuestionActivityBookmark extends QuestionActivityBase {

    // Danh sách ID câu hỏi đã được đánh dấu, theo thứ tự từ BookmarkActivity
    private List<Integer> bookmarkQuestionIds;
    // Vị trí hiện tại trong danh sách bookmarkQuestionIds
    private int currentBookmarkIndex = 0;

    // Biến để lưu vị trí trong Cursor (Không dùng trực tiếp, dùng để tính toán)
    // private int currentCursorPosition = 0; // Không cần thiết nếu ta dùng index.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_question_now);

            // 1. Ánh xạ UI components (Đảm bảo tất cả ID tồn tại)
            topicname = findViewById(R.id.topicname);
            back = findViewById(R.id.back);
            submit = findViewById(R.id.submit);
            content = findViewById(R.id.content);
            a = findViewById(R.id.a);
            b = findViewById(R.id.b);
            c = findViewById(R.id.c);
            d = findViewById(R.id.d);
            imgQuestion = findViewById(R.id.imgQuestion);
            radioGroup = findViewById(R.id.radioGroup);
            bookmarkButton = findViewById(R.id.bookmarkButton);

            init(); // Khởi tạo DB, Analytics

            id = "bookmark";

            // 2. Lấy danh sách ID câu hỏi và vị trí bắt đầu từ Intent
            if (intent != null && intent.hasExtra("list")) {
                bookmarkQuestionIds = (List<Integer>) intent.getSerializableExtra("list");
                currentBookmarkIndex = intent.getIntExtra("position", 0);
            }

            if (bookmarkQuestionIds == null || bookmarkQuestionIds.isEmpty()) {
                Toast.makeText(this, "Không có câu hỏi đánh dấu để hiển thị.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            count = bookmarkQuestionIds.size();

            // 3. Thiết lập Listener
            backSetup(this);
            submitSetup(this); // Gọi để ẩn nút submit
            setupBookmark(this);

            // 4. Thiết lập Cursor và hiển thị câu hỏi đầu tiên
            setCursor();

            if (cursor == null || cursor.getCount() == 0) {
                Toast.makeText(this, "Lỗi: Không tìm thấy câu hỏi trong Database local.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Di chuyển Cursor đến vị trí của ID đầu tiên và hiển thị
            showQuestionById(bookmarkQuestionIds.get(currentBookmarkIndex));

        }catch (Exception e) {
            Log.e("BOOKMARK_CRASH", "Lỗi khởi tạo Activity Bookmark", e);
            Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }


    @Override
    protected void setCursor() {
        if (bookmarkQuestionIds == null || bookmarkQuestionIds.isEmpty()) return;

        StringBuilder idList = new StringBuilder();
        for (int id : bookmarkQuestionIds) {
            if (idList.length() > 0) idList.append(",");
            idList.append(id);
        }

        // Dùng SELECT * vì QuestionActivityNow dùng và hoạt động
        // Sắp xếp theo ID để Cursor ổn định
        String sql = String.format(Locale.US,
                "SELECT * FROM Questions WHERE question_id IN (%s) ORDER BY question_id ASC",
                idList.toString());

        cursor = database.rawQuery(sql, null);
    }

    /**
     * TÌM ID TRONG CURSOR VÀ GỌI set_content.
     * Hàm này được gọi trong onCreate và khi chuyển câu (Next/Prev).
     * @param targetId ID câu hỏi cần hiển thị.
     */
    private void showQuestionById(int targetId) {
        if (cursor == null || cursor.getCount() == 0) return;

        topicname.setText(String.format("Bookmark (%d/%d) - ID: %d", currentBookmarkIndex + 1, count, targetId));

        if (cursor.moveToFirst()) {
            boolean found = false;
            do {
                try {
                    // Cột 0 là ID. Nếu QuestionActivityNow chạy đúng với SELECT *, cột 0 phải là ID.
                    if (cursor.getInt(0) == targetId) {
                        set_content(cursor, this); // Dòng này sẽ crash nếu dữ liệu sai kiểu
                        radioGroup.clearCheck();
                        setupBookmark(this); // Cập nhật trạng thái Bookmark
                        found = true;
                        break;
                    }
                } catch (Exception e) {
                    // Nếu lỗi xảy ra ở đây, đó là lỗi đọc kiểu dữ liệu.
                    Log.e("BOOKMARK_READ_ERROR", "Lỗi đọc/hiển thị câu hỏi ID " + targetId + ": " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi hiển thị câu hỏi ID " + targetId + ". Vui lòng kiểm tra Logcat.", Toast.LENGTH_LONG).show();
                    // KHÔNG finish() để có thể kiểm tra Logcat chi tiết lỗi set_content
                    found = true; // Coi như đã xử lý để thoát vòng lặp
                    break;
                }
            } while (cursor.moveToNext());

            if (!found) {
                Toast.makeText(this, "Lỗi: Không tìm thấy câu hỏi " + targetId + " trong DB.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // Logic điều hướng (Đã sửa để dùng index của danh sách bookmark)
    public void onNextQuestion(View view) {
        if (currentBookmarkIndex < bookmarkQuestionIds.size() - 1) {
            currentBookmarkIndex++;
            int nextId = bookmarkQuestionIds.get(currentBookmarkIndex);
            showQuestionById(nextId);
        } else {
            Toast.makeText(this, "Đã hết câu hỏi đã đánh dấu.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onPrevQuestion(View view) {
        if (currentBookmarkIndex > 0) {
            currentBookmarkIndex--;
            int prevId = bookmarkQuestionIds.get(currentBookmarkIndex);
            showQuestionById(prevId);
        } else {
            Toast.makeText(this, "Đây là câu hỏi đầu tiên trong danh sách đánh dấu.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void submitSetup(Context context) {
        // Trong chế độ Bookmark, ẩn nút Submit
        if (submit != null) {
            submit.setVisibility(View.GONE);
        }
    }

    @Override
    protected void logAttemptForCurrent() {
        // KHÔNG ghi log attempt trong chế độ Bookmark
    }
}