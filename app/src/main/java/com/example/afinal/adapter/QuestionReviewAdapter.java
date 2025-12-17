package com.example.afinal.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.afinal.R;
import com.example.afinal.dbclass.Question;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class QuestionReviewAdapter extends RecyclerView.Adapter<QuestionReviewAdapter.QuestionReviewViewHolder> {

    private Context context;
    private ArrayList<Question> questions;

    public QuestionReviewAdapter(Context context, ArrayList<Question> questions) {
        this.context = context;
        this.questions = questions;
    }

    @NonNull
    @Override
    public QuestionReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_question_review_card, parent, false);
        return new QuestionReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionReviewViewHolder holder, int position) {
        Question question = questions.get(position);
        holder.bind(question, position);
    }

    @Override
    public int getItemCount() {
        return questions != null ? questions.size() : 0;
    }

    class QuestionReviewViewHolder extends RecyclerView.ViewHolder {
        TextView txtQuestionNumber;
        TextView txtQuestionContent;
        ImageView imgQuestion;
        MaterialCardView chipStatus;
        TextView txtStatus;
        MaterialCardView badgeCritical;
        MaterialCardView cardOptionA, cardOptionB, cardOptionC, cardOptionD;
        TextView txtOptionA, txtOptionB, txtOptionC, txtOptionD;
        TextView txtExplanation;

        public QuestionReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            txtQuestionNumber = itemView.findViewById(R.id.txt_question_number);
            txtQuestionContent = itemView.findViewById(R.id.txt_question_content);
            imgQuestion = itemView.findViewById(R.id.img_question);
            chipStatus = itemView.findViewById(R.id.chip_status);
            txtStatus = itemView.findViewById(R.id.txt_status);
            badgeCritical = itemView.findViewById(R.id.badge_critical);
            cardOptionA = itemView.findViewById(R.id.card_option_a);
            cardOptionB = itemView.findViewById(R.id.card_option_b);
            cardOptionC = itemView.findViewById(R.id.card_option_c);
            cardOptionD = itemView.findViewById(R.id.card_option_d);
            txtOptionA = itemView.findViewById(R.id.txt_option_a);
            txtOptionB = itemView.findViewById(R.id.txt_option_b);
            txtOptionC = itemView.findViewById(R.id.txt_option_c);
            txtOptionD = itemView.findViewById(R.id.txt_option_d);
            txtExplanation = itemView.findViewById(R.id.txt_explanation);
        }

        public void bind(Question question, int position) {
            // Question number
            txtQuestionNumber.setText("Câu " + (position + 1));

            // Question content
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

            // Status and answer highlighting
            String correctAnswer = question.getAnswer();
            String userChoice = question.getUserChoice();

            // Reset all options
            resetOption(cardOptionA, txtOptionA);
            resetOption(cardOptionB, txtOptionB);
            resetOption(cardOptionC, txtOptionC);
            resetOption(cardOptionD, txtOptionD);

            // Highlight correct answer
            highlightCorrect(correctAnswer, question);

            // Highlight user's wrong choice
            if (userChoice != null && !userChoice.isEmpty() && !userChoice.equals(correctAnswer)) {
                highlightWrong(userChoice, question);
                chipStatus.setCardBackgroundColor(context.getResources().getColor(R.color.color_error));
                txtStatus.setText("Sai");
            } else if (userChoice == null || userChoice.isEmpty()) {
                chipStatus.setCardBackgroundColor(context.getResources().getColor(R.color.color_warning));
                txtStatus.setText("Chưa chọn");
            } else {
                chipStatus.setCardBackgroundColor(context.getResources().getColor(R.color.color_success));
                txtStatus.setText("Đúng");
            }

            // Explanation
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
                    InputStream inputStream = context.getAssets().open(imgPath);
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

        private void resetOption(MaterialCardView card, TextView text) {
            card.setCardBackgroundColor(context.getResources().getColor(R.color.color_surface));
            card.setStrokeWidth(2);
            card.setStrokeColor(context.getResources().getColor(R.color.color_outline));
            text.setTextColor(context.getResources().getColor(R.color.color_text_primary));
        }

        private void highlightCorrect(String answer, Question question) {
            MaterialCardView card = null;
            TextView text = null;
            
            if (answer.equals(question.getA())) {
                card = cardOptionA;
                text = txtOptionA;
            } else if (answer.equals(question.getB())) {
                card = cardOptionB;
                text = txtOptionB;
            } else if (answer.equals(question.getC())) {
                card = cardOptionC;
                text = txtOptionC;
            } else if (answer.equals(question.getD())) {
                card = cardOptionD;
                text = txtOptionD;
            }

            if (card != null && text != null) {
                card.setCardBackgroundColor(context.getResources().getColor(R.color.color_success));
                card.setStrokeWidth(0);
                text.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            }
        }

        private void highlightWrong(String userChoice, Question question) {
            MaterialCardView card = null;
            TextView text = null;
            
            if (userChoice.equals(question.getA())) {
                card = cardOptionA;
                text = txtOptionA;
            } else if (userChoice.equals(question.getB())) {
                card = cardOptionB;
                text = txtOptionB;
            } else if (userChoice.equals(question.getC())) {
                card = cardOptionC;
                text = txtOptionC;
            } else if (userChoice.equals(question.getD())) {
                card = cardOptionD;
                text = txtOptionD;
            }

            if (card != null && text != null) {
                card.setCardBackgroundColor(context.getResources().getColor(R.color.color_error));
                card.setStrokeWidth(0);
                text.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            }
        }
    }
}

