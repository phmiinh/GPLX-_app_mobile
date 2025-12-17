package com.example.afinal.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.afinal.R;
import com.example.afinal.dbclass.Question;

import java.util.ArrayList;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder> {

    private Context context;
    private ArrayList<Question> questions;
    private OnBookmarkClickListener listener;

    public interface OnBookmarkClickListener {
        void onBookmarkClick(Question question, int position);
    }

    public BookmarkAdapter(Context context, ArrayList<Question> questions, OnBookmarkClickListener listener) {
        this.context = context;
        this.questions = questions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bookmark_card, parent, false);
        return new BookmarkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
        Question question = questions.get(position);
        holder.bind(question, position);
    }

    @Override
    public int getItemCount() {
        return questions != null ? questions.size() : 0;
    }

    class BookmarkViewHolder extends RecyclerView.ViewHolder {
        TextView txtQuestionNumber;
        TextView txtQuestion;
        TextView txtCategory;

        public BookmarkViewHolder(@NonNull View itemView) {
            super(itemView);
            txtQuestionNumber = itemView.findViewById(R.id.txt_question_number);
            txtQuestion = itemView.findViewById(R.id.txt_question);
            txtCategory = itemView.findViewById(R.id.txt_category);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onBookmarkClick(questions.get(position), position);
                    }
                }
            });
        }

        public void bind(Question question, int position) {
            txtQuestionNumber.setText(String.valueOf(position + 1));
            txtQuestion.setText(question.getContent());
            
            // Show category if available (you might need to add category info to Question model)
            String category = "Câu hỏi đã đánh dấu";
            if (question.getIs_critical() == 1) {
                category += " • Điểm liệt";
            }
            txtCategory.setText(category);
        }
    }
}

