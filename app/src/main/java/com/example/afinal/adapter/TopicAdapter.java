package com.example.afinal.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.afinal.R;
import com.example.afinal.dbclass.Categories;

import java.util.ArrayList;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicViewHolder> {

    private Context context;
    private ArrayList<Categories> topics;
    private OnTopicClickListener listener;

    public interface OnTopicClickListener {
        void onTopicClick(Categories category);
    }

    public TopicAdapter(Context context, ArrayList<Categories> topics, OnTopicClickListener listener) {
        this.context = context;
        this.topics = topics;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_topic_card, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        Categories topic = topics.get(position);
        holder.bind(topic);
    }

    @Override
    public int getItemCount() {
        return topics != null ? topics.size() : 0;
    }

    class TopicViewHolder extends RecyclerView.ViewHolder {
        TextView txtTopicName;
        TextView txtNumQuestions;

        public TopicViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTopicName = itemView.findViewById(R.id.txt_topic_name);
            txtNumQuestions = itemView.findViewById(R.id.txt_num_questions);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onTopicClick(topics.get(position));
                    }
                }
            });
        }

        public void bind(Categories topic) {
            txtTopicName.setText(topic.getName());
            txtNumQuestions.setText(topic.getNum() + " câu hỏi");
        }
    }
}

