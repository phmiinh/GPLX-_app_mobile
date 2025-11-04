package com.example.afinal;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class QuestionActivityNow extends QuestionActivityBase {

    // Đây là Activity cho chế độ "Xem đáp án ngay lập tức"
    private int currentQuestionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_now);

        // 1. Ánh xạ các UI components
        topicname = findViewById(R.id.topicname);
        back = findViewById(R.id.back);
        submit = findViewById(R.id.submit);
        content = findViewById(R.id.content);
        imgQuestion = findViewById(R.id.imgQuestion);
        radioGroup = findViewById(R.id.radioGroup);

        a = findViewById(R.id.a);
        b = findViewById(R.id.b);
        c = findViewById(R.id.c);
        d = findViewById(R.id.d);

        // 2. Ánh xạ NÚT BOOKMARK MỚI
        bookmarkButton = findViewById(R.id.bookmarkButton);

        // Khởi tạo Base (DB, Intent, Analytics)
        init();

        // Tải danh sách câu hỏi
        setCursor();

        // Thiết lập các Listener
        backSetup(this);
        submitSetup(this);

        // Lấy câu hỏi đầu tiên
        if (cursor != null && cursor.moveToFirst()) {
            set_content(cursor, this);

            // THIẾT LẬP BOOKMARK CHO CÂU HỎI HIỆN TẠI (LẦN ĐẦU)
            setupBookmark(this);
        } else {
            Toast.makeText(this, "Không có câu hỏi để hiển thị.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // Logic điều hướng (Được gọi từ onClick="onNextQuestion" trong XML)
    public void onNextQuestion(View view) {
        // Ghi log nỗ lực cho câu hỏi hiện tại trước khi chuyển câu
        logAttemptForCurrent();

        if (cursor != null && cursor.moveToNext()) {
            set_content(cursor, this);
            // Xóa lựa chọn cũ khi chuyển câu
            radioGroup.clearCheck();

            // CẬP NHẬT TRẠNG THÁI BOOKMARK CHO CÂU HỎI MỚI
            setupBookmark(this);
        } else {
            Toast.makeText(this, "Bạn đã hoàn thành các câu hỏi trong mục này.", Toast.LENGTH_SHORT).show();
            // Tùy chọn: Gọi showpoint(this) nếu muốn hiển thị kết quả tổng (khi hết bài)
        }
    }

    // Hàm onPrevQuestion (Nếu bạn thêm nút "Câu trước" vào layout)
    public void onPrevQuestion(View view) {
        // Ghi log nỗ lực cho câu hỏi hiện tại trước khi chuyển câu (Nếu bạn muốn ghi nhận luôn)
        logAttemptForCurrent();

        if (cursor != null && cursor.moveToPrevious()) {
            set_content(cursor, this);
            // Logic khôi phục lựa chọn cũ cần được thêm vào nếu cần

            // CẬP NHẬT TRẠNG THÁI BOOKMARK CHO CÂU HỎI MỚI
            setupBookmark(this);
        } else {
            Toast.makeText(this, "Đây là câu hỏi đầu tiên.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void submitSetup(Context context) {
        // Giữ lại logic submit mặc định của Base.
        super.submitSetup(this);
    }
}
