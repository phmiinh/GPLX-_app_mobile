package com.example.afinal.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.afinal.R;
import com.example.afinal.dbclass.Level;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class LevelRecyclerAdapter extends RecyclerView.Adapter<LevelRecyclerAdapter.LevelViewHolder> {

    private Context context;
    private ArrayList<Level> levels;
    private OnLevelClickListener listener;

    public interface OnLevelClickListener {
        void onLevelClick(Level level);
    }

    public LevelRecyclerAdapter(Context context, ArrayList<Level> levels, OnLevelClickListener listener) {
        this.context = context;
        this.levels = levels;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_level_card, parent, false);
        return new LevelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
        Level level = levels.get(position);
        holder.bind(level);
    }

    @Override
    public int getItemCount() {
        return levels != null ? levels.size() : 0;
    }

    class LevelViewHolder extends RecyclerView.ViewHolder {
        TextView txtLevelName;
        TextView txtLevelTitle;
        TextView txtLevelRequirements;
        TextView txtLevelTime;
        MaterialButton btnStartExam;

        public LevelViewHolder(@NonNull View itemView) {
            super(itemView);
            txtLevelName = itemView.findViewById(R.id.txt_level_name);
            txtLevelTitle = itemView.findViewById(R.id.txt_level_title);
            txtLevelRequirements = itemView.findViewById(R.id.txt_level_requirements);
            txtLevelTime = itemView.findViewById(R.id.txt_level_time);
            btnStartExam = itemView.findViewById(R.id.btn_start_exam);
        }

        public void bind(Level level) {
            // Extract level name (e.g., "B2" from "Hạng B2")
            String levelName = level.getName().replace("Hạng ", "");
            txtLevelName.setText(levelName);
            txtLevelTitle.setText(level.getName());
            txtLevelRequirements.setText(String.format("Cần đúng ít nhất %d/%d câu", 
                level.getMinRequired(), level.getTotalQuestion()));
            txtLevelTime.setText(String.format("Thời gian: %d phút", level.getTime()));

            // Click listener on button
            btnStartExam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onLevelClick(levels.get(position));
                    }
                }
            });

            // Also allow card click
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onLevelClick(levels.get(position));
                    }
                }
            });
        }
    }
}

