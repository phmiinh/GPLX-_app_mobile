package com.example.afinal;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.afinal.dbclass.Question;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;
import java.io.InputStream;

public class QuestionDetailActivity extends BaseNavigationActivity {
    private Question question;
    private TextView txtQuestionContent;
    private ImageView imgQuestion;
    private MaterialCardView badgeCritical;
    private MaterialCardView cardOptionA, cardOptionB, cardOptionC, cardOptionD;
    private TextView txtOptionA, txtOptionB, txtOptionC, txtOptionD;
    private TextView txtExplanation;
    private MaterialCardView cardAiExplanation;
    private MaterialButton btnAiExplain;
    private ProgressBar progressAi;
    private TextView txtAiExplanation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // Setup navigation
        setupToolbar(false, "Chi tiết câu hỏi");

        // Get question from intent
        question = getIntent().getParcelableExtra("question");
        if (question == null) {
            android.util.Log.e("QuestionDetailActivity", "Question is null from intent");
            Toast.makeText(this, "Không tìm thấy câu hỏi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        android.util.Log.d("QuestionDetailActivity", "Question loaded: " + question.getContent());

        // Initialize views
        initViews();
        
        // Bind question data
        bindQuestion();

        // Setup AI explanation button
        btnAiExplain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateAiExplanation();
            }
        });
    }

    private void initViews() {
        txtQuestionContent = findViewById(R.id.txt_question_content);
        imgQuestion = findViewById(R.id.img_question);
        badgeCritical = findViewById(R.id.badge_critical);
        cardOptionA = findViewById(R.id.card_option_a);
        cardOptionB = findViewById(R.id.card_option_b);
        cardOptionC = findViewById(R.id.card_option_c);
        cardOptionD = findViewById(R.id.card_option_d);
        txtOptionA = findViewById(R.id.txt_option_a);
        txtOptionB = findViewById(R.id.txt_option_b);
        txtOptionC = findViewById(R.id.txt_option_c);
        txtOptionD = findViewById(R.id.txt_option_d);
        txtExplanation = findViewById(R.id.txt_explanation);
        cardAiExplanation = findViewById(R.id.card_ai_explanation);
        btnAiExplain = findViewById(R.id.btn_ai_explain);
        progressAi = findViewById(R.id.progress_ai);
        txtAiExplanation = findViewById(R.id.txt_ai_explanation);
    }

    private void bindQuestion() {
        // Question content (không hiển thị số câu vì đây là detail screen)
        String content = question.getContent();
        if (question.getIs_critical() == 1) {
            badgeCritical.setVisibility(View.VISIBLE);
            content = "(Câu điểm liệt) " + content;
        } else {
            badgeCritical.setVisibility(View.GONE);
        }
        txtQuestionContent.setText(content);

        // Options
        txtOptionA.setText("A. " + question.getA());
        txtOptionB.setText("B. " + question.getB());
        
        if (question.getC() != null && !question.getC().isEmpty()) {
            cardOptionC.setVisibility(View.VISIBLE);
            txtOptionC.setText("C. " + question.getC());
        } else {
            cardOptionC.setVisibility(View.GONE);
        }

        if (question.getD() != null && !question.getD().isEmpty()) {
            cardOptionD.setVisibility(View.VISIBLE);
            txtOptionD.setText("D. " + question.getD());
        } else {
            cardOptionD.setVisibility(View.GONE);
        }

        // Highlight correct answer
        highlightCorrectAnswer();

        // Standard explanation
        String explanation = question.getExplain();
        if (explanation != null && !explanation.isEmpty()) {
            txtExplanation.setText(explanation);
        } else {
            txtExplanation.setText("Chưa có giải thích.");
        }

        // Image
        if (question.getImg_url() != null && !question.getImg_url().isEmpty()) {
            try {
                String imgPath = "img/" + question.getImg_url() + ".png";
                InputStream inputStream = getAssets().open(imgPath);
                Drawable drawable = Drawable.createFromStream(inputStream, null);
                inputStream.close();
                imgQuestion.setImageDrawable(drawable);
                imgQuestion.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                imgQuestion.setVisibility(View.GONE);
            }
        } else {
            imgQuestion.setVisibility(View.GONE);
        }
    }

    private void highlightCorrectAnswer() {
        // Reset all options
        resetOption(cardOptionA, txtOptionA);
        resetOption(cardOptionB, txtOptionB);
        resetOption(cardOptionC, txtOptionC);
        resetOption(cardOptionD, txtOptionD);

        // Highlight correct answer
        String correctAnswer = question.getAnswer();
        MaterialCardView correctCard = null;
        TextView correctText = null;

        if (correctAnswer.equals(question.getA())) {
            correctCard = cardOptionA;
            correctText = txtOptionA;
        } else if (correctAnswer.equals(question.getB())) {
            correctCard = cardOptionB;
            correctText = txtOptionB;
        } else if (correctAnswer.equals(question.getC())) {
            correctCard = cardOptionC;
            correctText = txtOptionC;
        } else if (correctAnswer.equals(question.getD())) {
            correctCard = cardOptionD;
            correctText = txtOptionD;
        }

        if (correctCard != null && correctText != null) {
            correctCard.setCardBackgroundColor(getResources().getColor(R.color.color_success));
            correctCard.setStrokeWidth(0);
            correctText.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        }
    }

    private void resetOption(MaterialCardView card, TextView text) {
        card.setCardBackgroundColor(getResources().getColor(R.color.color_surface));
        card.setStrokeWidth(2);
        card.setStrokeColor(getResources().getColor(R.color.color_outline));
        text.setTextColor(getResources().getColor(R.color.color_text_primary));
    }

    private void generateAiExplanation() {
        btnAiExplain.setEnabled(false);
        progressAi.setVisibility(View.VISIBLE);
        txtAiExplanation.setVisibility(View.GONE);

        // Build prompt for AI
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Giải thích chi tiết câu hỏi sau về luật giao thông đường bộ:\n\n");
        promptBuilder.append("Câu hỏi: ").append(question.getContent()).append("\n\n");
        promptBuilder.append("A. ").append(question.getA()).append("\n");
        promptBuilder.append("B. ").append(question.getB()).append("\n");
        if (question.getC() != null) {
            promptBuilder.append("C. ").append(question.getC()).append("\n");
        }
        if (question.getD() != null) {
            promptBuilder.append("D. ").append(question.getD()).append("\n");
        }
        promptBuilder.append("\nĐáp án đúng: ").append(question.getAnswer()).append("\n\n");
        promptBuilder.append("Hãy giải thích tại sao đáp án này đúng và các đáp án khác sai.");

        // Run AI call in background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GeminiApiManager apiManager = new GeminiApiManager();
                    String aiExplanation = apiManager.generateExplanation(promptBuilder.toString());

                    // Update UI on main thread
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            progressAi.setVisibility(View.GONE);
                            btnAiExplain.setEnabled(true);
                            txtAiExplanation.setText(aiExplanation);
                            txtAiExplanation.setVisibility(View.VISIBLE);
                        }
                    });
                } catch (Exception e) {
                    // Handle API errors gracefully
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            progressAi.setVisibility(View.GONE);
                            btnAiExplain.setEnabled(true);
                            String errorMsg = "Không thể tạo giải thích AI. ";
                            if (e.getMessage() != null && e.getMessage().contains("GEMINI_API_KEY")) {
                                errorMsg += "Vui lòng cấu hình API key trong local.properties.";
                            } else {
                                errorMsg += "Vui lòng thử lại sau.";
                            }
                            txtAiExplanation.setText(errorMsg);
                            txtAiExplanation.setVisibility(View.VISIBLE);
                            Toast.makeText(QuestionDetailActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }
}

