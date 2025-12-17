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

import java.util.List;
import java.util.Locale;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    public static class LeaderboardRow {
        public int rank;
        public String displayName;
        public long bestScore;
        public double avgScore;
        public int examCount;

        public LeaderboardRow(int rank, String displayName, long bestScore, double avgScore, int examCount) {
            this.rank = rank;
            this.displayName = displayName;
            this.bestScore = bestScore;
            this.avgScore = avgScore;
            this.examCount = examCount;
        }
    }

    private Context context;
    private List<LeaderboardRow> rows;

    public LeaderboardAdapter(Context context, List<LeaderboardRow> rows) {
        this.context = context;
        this.rows = rows;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_leaderboard_card, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        LeaderboardRow row = rows.get(position);
        holder.bind(row);
    }

    @Override
    public int getItemCount() {
        return rows != null ? rows.size() : 0;
    }

    class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        TextView txtRank;
        TextView txtDisplayName;
        TextView txtStats;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            txtRank = itemView.findViewById(R.id.txt_rank);
            txtDisplayName = itemView.findViewById(R.id.txt_display_name);
            txtStats = itemView.findViewById(R.id.txt_stats);
        }

        public void bind(LeaderboardRow row) {
            txtRank.setText(String.valueOf(row.rank));
            txtDisplayName.setText(row.displayName != null && !row.displayName.isEmpty() 
                ? row.displayName 
                : "User #" + row.rank);
            
            String stats = String.format(Locale.getDefault(),
                "Best: %d • Avg: %.1f • %d lần thi",
                row.bestScore, row.avgScore, row.examCount);
            txtStats.setText(stats);
        }
    }
}

