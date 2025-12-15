package com.example.afinal;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.afinal.DAO.CategoriesDAO;
import com.example.afinal.DAO.QuestionDAO;
import com.example.afinal.dbclass.Categories;
import com.example.afinal.dbclass.Question;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entry screen for AI-powered smart practice and mock exams.
 * Lets the user pick mode, topic and number of questions, then calls
 * the recommendation service and starts the appropriate question flow.
 */
public class SmartPracticeActivity extends AppCompatActivity {

    private Spinner modeSpinner;
    private Spinner topicSpinner;
    private Spinner countSpinner;
    private CheckBox criticalOnlyCheck;
    private Button startButton;
    private ProgressBar progressBar;
    private TextView metaSummary;

    private SQLiteDatabase database;
    private CategoriesDAO categoriesDAO;
    private QuestionDAO questionDAO;
    private AiRecommendationClient client;

    private List<Categories> topics = new ArrayList<>();
    private Map<String, Integer> topicIdByName = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_smart_practice);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = openOrCreateDatabase("ATGT.db", MODE_PRIVATE, null);
        categoriesDAO = new CategoriesDAO(database);
        questionDAO = new QuestionDAO(database);
        client = new AiRecommendationClient();

        bindViews();
        setupModeSpinner();
        setupTopicSpinner();
        setupCountSpinner();
        setupStartButton();
    }

    private void bindViews() {
        modeSpinner = findViewById(R.id.sp_mode);
        topicSpinner = findViewById(R.id.sp_topic);
        countSpinner = findViewById(R.id.sp_count);
        criticalOnlyCheck = findViewById(R.id.cb_critical_only);
        startButton = findViewById(R.id.btn_start_smart);
        progressBar = findViewById(R.id.pb_loading);
        metaSummary = findViewById(R.id.txt_meta_summary);
    }

    private void setupModeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Ôn thông minh (xem đáp án ngay)", "Thi thử thông minh (giống đề thật)"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpinner.setAdapter(adapter);
    }

    private void setupTopicSpinner() {
        topics = categoriesDAO.getAllCategories();
        List<String> labels = new ArrayList<>();
        labels.add("Tất cả chủ đề");
        topicIdByName.clear();
        for (Categories c : topics) {
            labels.add(c.getName());
            topicIdByName.put(c.getName(), c.getId());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                labels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        topicSpinner.setAdapter(adapter);
    }

    private void setupCountSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"10", "20", "30", "60"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countSpinner.setAdapter(adapter);
    }

    private void setupStartButton() {
        startButton.setOnClickListener(v -> {
            String modeLabel = (String) modeSpinner.getSelectedItem();
            final String mode = modeLabel.startsWith("Thi thử") ? "ai_mock_exam" : "ai_practice";

            String topicLabel = (String) topicSpinner.getSelectedItem();
            Integer topicId = null;
            if (topicLabel != null && topicIdByName.containsKey(topicLabel)) {
                topicId = topicIdByName.get(topicLabel);
            }

            int numQuestions = Integer.parseInt((String) countSpinner.getSelectedItem());
            boolean criticalOnly = criticalOnlyCheck.isChecked();

            requestAndStart(mode, topicId, numQuestions, criticalOnly);
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        startButton.setEnabled(!loading);
    }

    private void requestAndStart(String mode, Integer topicId, int numQuestions, boolean criticalOnly) {
        setLoading(true);
        metaSummary.setText("");

        client.requestRecommendations(
                this,
                mode,
                topicId,
                numQuestions,
                criticalOnly,
                new AiRecommendationClient.RecommendationCallback() {
                    @Override
                    public void onSuccess(List<String> ids, List<Map<String, Object>> metadata) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            if (ids == null || ids.isEmpty()) {
                                Toast.makeText(SmartPracticeActivity.this, "Chưa đủ dữ liệu để gợi ý ôn thông minh. Hãy làm thêm vài đề trước nhé.", Toast.LENGTH_LONG).show();
                                return;
                            }

                            ArrayList<Question> questions = questionDAO.getQuestionsByIds(ids);
                            if (questions.isEmpty()) {
                                Toast.makeText(SmartPracticeActivity.this, "Không tải được danh sách câu hỏi từ cơ sở dữ liệu.", Toast.LENGTH_LONG).show();
                                return;
                            }

                            updateMetaSummary(metadata);

                            if ("ai_mock_exam".equals(mode)) {
                                launchMockExamAi(questions);
                            } else {
                                launchPracticeAi(questions);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(SmartPracticeActivity.this, "Không tải được danh sách ôn thông minh. Vui lòng kiểm tra mạng và thử lại.", Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }

    private void updateMetaSummary(List<Map<String, Object>> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            metaSummary.setText("");
            return;
        }
        int total = metadata.size();
        int critical = 0;
        int highRisk = 0;
        for (Map<String, Object> m : metadata) {
            boolean isCritical = (Boolean) m.getOrDefault("is_critical", false);
            double pCorrect = (Double) m.getOrDefault("predicted_correct_prob", 0.0);
            double urgency = (Double) m.getOrDefault("urgency_score", 0.0);
            if (isCritical) critical++;
            if (pCorrect < 0.6 || urgency > 0.5) {
                highRisk++;
            }
        }
        String summary = "Đề xuất: " + total +
                " câu • Câu điểm liệt: " + critical +
                " • Câu nguy cơ sai cao: " + highRisk;
        metaSummary.setText(summary);
    }

    private void launchPracticeAi(ArrayList<Question> questions) {
        Intent intent = new Intent(this, QuestionActivityNow.class);
        intent.putExtra("id", "topic");
        intent.putExtra("ai_mode", "ai_practice");
        intent.putExtra("categories_id", 0);
        intent.putExtra("start", 1);
        intent.putExtra("end", questions.size());
        // Pass list via static holder is simpler here; for now reuse DB-based loading.
        // A more robust version would write the subset into a temp table keyed by session id.
        QuestionHolder.setQuestions(questions);
        startActivity(intent);
    }

    private void launchMockExamAi(ArrayList<Question> questions) {
        Intent intent = new Intent(this, QuestionActivityLast.class);
        intent.putExtra("id", "level");
        intent.putExtra("ai_mode", "ai_mock_exam");
        intent.putExtra("level_id", -1);
        intent.putExtra("min", 0);
        intent.putExtra("total", questions.size());
        intent.putExtra("name", "Thi thử thông minh (AI)");
        intent.putExtra("time", 20); // mặc định 20 phút cho thi thử AI
        QuestionHolder.setQuestions(questions);
        startActivity(intent);
    }
}


