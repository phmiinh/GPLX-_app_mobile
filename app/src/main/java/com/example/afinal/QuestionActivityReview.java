package com.example.afinal;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.afinal.adapter.QuestionReviewAdapter;
import com.example.afinal.dbclass.Question;

import java.util.ArrayList;

public class QuestionActivityReview extends BaseNavigationActivity {
    private RecyclerView rvReviewQuestions;
    private TextView txtReviewStats;
    private TextView txtReviewTime;
    private ArrayList<Question> listQuestion;
    private String result, startTime, endTime, time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_review_new);

        // Setup navigation
        setupToolbar(false, "Xem lại bài làm");

        // Initialize views
        rvReviewQuestions = findViewById(R.id.rv_review_questions);
        txtReviewStats = findViewById(R.id.txt_review_stats);
        txtReviewTime = findViewById(R.id.txt_review_time);

        // Get data from intent
        listQuestion = getIntent().getParcelableArrayListExtra("listQuestion");
        result = getIntent().getStringExtra("result");
        startTime = getIntent().getStringExtra("startTime");
        endTime = getIntent().getStringExtra("endTime");
        time = getIntent().getStringExtra("time");

        if (listQuestion == null) {
            listQuestion = new ArrayList<>();
        }

        // Setup stats
        setInfo();

        // Setup RecyclerView
        QuestionReviewAdapter adapter = new QuestionReviewAdapter(this, listQuestion);
        rvReviewQuestions.setAdapter(adapter);

        // Setup done button
        findViewById(R.id.btn_review_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setInfo() {
        if (result != null) {
            txtReviewStats.setText("Kết quả: " + result);
        }
        if (startTime != null && endTime != null) {
            txtReviewTime.setText("Từ: " + startTime + " đến " + endTime);
        }
    }
}
