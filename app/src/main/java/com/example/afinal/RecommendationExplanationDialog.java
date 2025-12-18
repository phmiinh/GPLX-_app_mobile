package com.example.afinal;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

/**
 * Dialog showing explanation for why a question was recommended
 */
public class RecommendationExplanationDialog extends Dialog {
    private final Map<String, Object> metadata;

    public RecommendationExplanationDialog(@NonNull Context context, Map<String, Object> metadata) {
        super(context);
        this.metadata = metadata;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_recommendation_explanation);

        TextView title = findViewById(R.id.txt_explanation_title);
        TextView reasons = findViewById(R.id.txt_reasons);
        TextView scores = findViewById(R.id.txt_scores);
        Button closeBtn = findViewById(R.id.btn_close);

        title.setText("Vì sao câu này được chọn?");

        // Build reasons text
        @SuppressWarnings("unchecked")
        List<String> reasonsList = (List<String>) metadata.get("reasons");
        if (reasonsList != null && !reasonsList.isEmpty()) {
            reasons.setText(String.join("\n• ", reasonsList));
            reasons.setVisibility(View.VISIBLE);
        } else {
            reasons.setVisibility(View.GONE);
        }

        // Build scores text
        StringBuilder scoresText = new StringBuilder();
        double priorityScore = ((Number) metadata.getOrDefault("priority_score", 0.0)).doubleValue();
        double pCorrect = ((Number) metadata.getOrDefault("predicted_correct_prob", 0.5)).doubleValue();
        double urgency = ((Number) metadata.getOrDefault("urgency_score", 0.0)).doubleValue();

        scoresText.append("Điểm ưu tiên: ").append(String.format("%.2f", priorityScore)).append("\n");
        scoresText.append("Xác suất đúng: ").append(String.format("%.1f%%", pCorrect * 100)).append("\n");
        scoresText.append("Độ khẩn cấp: ").append(String.format("%.2f", urgency));

        scores.setText(scoresText.toString());

        closeBtn.setOnClickListener(v -> dismiss());
    }
}

