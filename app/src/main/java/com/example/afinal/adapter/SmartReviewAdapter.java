package com.example.afinal.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.afinal.R;
import com.example.afinal.SmartReviewActivity;

import java.util.List;

public class SmartReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<SmartReviewActivity.DueQuestionItem> items;
    private final OnQuestionClickListener listener;

    public interface OnQuestionClickListener {
        void onQuestionClick(String questionId);
    }

    public SmartReviewAdapter(List<SmartReviewActivity.DueQuestionItem> items, OnQuestionClickListener listener) {
        this.items = items != null ? items : new java.util.ArrayList<>();
        this.listener = listener;
    }

    public void updateItems(List<SmartReviewActivity.DueQuestionItem> newItems) {
        if (newItems == null) {
            this.items = new java.util.ArrayList<>();
        } else {
            this.items = new java.util.ArrayList<>(newItems);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).isSectionHeader ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_due_section_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_due_question, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SmartReviewActivity.DueQuestionItem item = items.get(position);
        if (item.isSectionHeader) {
            ((HeaderViewHolder) holder).bind(item);
        } else {
            ((ItemViewHolder) holder).bind(item, listener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView count;

        HeaderViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.txt_section_title);
            count = view.findViewById(R.id.txt_section_count);
        }

        void bind(SmartReviewActivity.DueQuestionItem item) {
            title.setText(item.sectionTitle);
            count.setText(String.valueOf(item.sectionCount));
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView questionId;
        private final TextView badges;
        private final TextView reasons;

        ItemViewHolder(View view) {
            super(view);
            questionId = view.findViewById(R.id.txt_question_id);
            badges = view.findViewById(R.id.txt_badges);
            reasons = view.findViewById(R.id.txt_reasons);
        }

        void bind(SmartReviewActivity.DueQuestionItem item, OnQuestionClickListener listener) {
            questionId.setText("Câu hỏi #" + item.questionId);

            // Build badges
            StringBuilder badgeText = new StringBuilder();
            if (item.isCritical) {
                badgeText.append("Câu liệt ");
            }
            if (item.predictedCorrectProb < 0.6) {
                badgeText.append("Nguy cơ cao ");
            }
            if (item.dueTimeMs > 0) {
                badgeText.append("Đến hạn ");
            }
            badges.setText(badgeText.toString().trim());
            badges.setVisibility(badgeText.length() > 0 ? View.VISIBLE : View.GONE);

            // Build reasons
            if (item.reasons != null && !item.reasons.isEmpty()) {
                reasons.setText(String.join(" • ", item.reasons));
                reasons.setVisibility(View.VISIBLE);
            } else {
                reasons.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuestionClick(item.questionId);
                }
            });
        }
    }
}

