package com.example.afinal.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.afinal.R;

import java.util.List;

public class BookmarkListAdapter extends RecyclerView.Adapter<BookmarkListAdapter.BookmarkViewHolder> {

    private final Context context;
    private List<Integer> questionIds;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int questionId, int position);
    }

    public BookmarkListAdapter(Context context, List<Integer> questionIds, OnItemClickListener listener) {
        this.context = context;
        this.questionIds = questionIds;
        this.listener = listener;
    }

    public void setQuestionIds(List<Integer> newQuestionIds) {
        this.questionIds = newQuestionIds;
        notifyDataSetChanged();
    }

    public List<Integer> getQuestionIds() {
        return questionIds;
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bookmark, parent, false);
        return new BookmarkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
        int questionId = questionIds.get(position);

        // Hiển thị ID và thông tin
        holder.tvQuestionId.setText("Câu hỏi số " + questionId);
        holder.tvTopic.setText("Đã đánh dấu");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(questionId, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return questionIds.size();
    }

    public static class BookmarkViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestionId;
        TextView tvTopic;

        public BookmarkViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestionId = itemView.findViewById(R.id.tvQuestionId);
            tvTopic = itemView.findViewById(R.id.tvTopic);
        }
    }
}
