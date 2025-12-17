package com.example.afinal.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.afinal.R;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    public static class HistoryItem {
        public String examName;
        public long submittedAt;
        public int scoreRaw;
        public int totalQuestions;
        public boolean passed;

        public HistoryItem(String examName, long submittedAt, int scoreRaw, int totalQuestions, boolean passed) {
            this.examName = examName;
            this.submittedAt = submittedAt;
            this.scoreRaw = scoreRaw;
            this.totalQuestions = totalQuestions;
            this.passed = passed;
        }
    }

    private Context context;
    private List<HistoryItem> items;
    private SimpleDateFormat dateFormat;

    public HistoryAdapter(Context context, List<HistoryItem> items) {
        this.context = context;
        this.items = items;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history_card, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtExamName;
        TextView txtDateTime;
        TextView txtScore;
        MaterialCardView badgeStatus;
        TextView txtStatus;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtExamName = itemView.findViewById(R.id.txt_exam_name);
            txtDateTime = itemView.findViewById(R.id.txt_date_time);
            txtScore = itemView.findViewById(R.id.txt_score);
            badgeStatus = itemView.findViewById(R.id.badge_status);
            txtStatus = itemView.findViewById(R.id.txt_status);
        }

        public void bind(HistoryItem item) {
            txtExamName.setText(item.examName != null ? item.examName : "Bài thi");
            txtDateTime.setText(dateFormat.format(new Date(item.submittedAt)));
            txtScore.setText(String.format("%d/%d", item.scoreRaw, item.totalQuestions));
            
            if (item.passed) {
                txtStatus.setText("Đạt");
                badgeStatus.setCardBackgroundColor(context.getResources().getColor(R.color.color_success));
            } else {
                txtStatus.setText("Không đạt");
                badgeStatus.setCardBackgroundColor(context.getResources().getColor(R.color.color_error));
            }
        }
    }
}

