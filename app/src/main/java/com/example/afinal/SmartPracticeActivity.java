package com.example.afinal;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.afinal.DAO.CategoriesDAO;
import com.example.afinal.DAO.QuestionDAO;
import com.example.afinal.analytics.AnalyticsHelper;
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
public class SmartPracticeActivity extends BaseNavigationActivity {

    private Spinner modeSpinner;
    private Spinner topicSpinner;
    private Spinner countSpinner;
    private CheckBox criticalOnlyCheck;
    private Button startButton;
    private Button btnDueList;
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
        setContentView(R.layout.activity_smart_practice);

        // Setup navigation toolbar
        setupToolbar(false, "Ôn thông minh (AI)");
        enableBottomBar(3); // AI tab (0=Home, 1=Practice, 2=Mock, 3=AI)

        database = openOrCreateDatabase("ATGT.db", MODE_PRIVATE, null);
        categoriesDAO = new CategoriesDAO(database);
        questionDAO = new QuestionDAO(database);
        client = new AiRecommendationClient();
        
        // Initialize Analytics
        AnalyticsHelper.initialize(this);

        bindViews();
        setupModeSpinner();
        setupTopicSpinner();
        setupCountSpinner();
        setupStartButton();
        setupDueListButton();
    }

    private void bindViews() {
        modeSpinner = findViewById(R.id.sp_mode);
        topicSpinner = findViewById(R.id.sp_topic);
        countSpinner = findViewById(R.id.sp_count);
        criticalOnlyCheck = findViewById(R.id.cb_critical_only);
        startButton = findViewById(R.id.btn_start_smart);
        btnDueList = findViewById(R.id.btn_due_list);
        progressBar = findViewById(R.id.pb_loading);
        metaSummary = findViewById(R.id.txt_meta_summary);
    }

    private void setupDueListButton() {
        if (btnDueList != null) {
            btnDueList.setOnClickListener(v -> {
                AnalyticsHelper.logAIDueListViewViewed(this);
                Intent intent = new Intent(this, SmartReviewActivity.class);
                startActivity(intent);
            });
        }
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

            // Log analytics
            AnalyticsHelper.logAIPracticeStarted(this, mode, topicId, numQuestions, criticalOnly);
            if (criticalOnly) {
                AnalyticsHelper.logAITogglePrioritizeWeakChanged(this, true);
            }

            requestAndStart(mode, topicId, numQuestions, criticalOnly);
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        startButton.setEnabled(!loading);
    }

    private void requestAndStart(String mode, Integer topicId, int numQuestions, boolean criticalOnly) {
        Log.d("SmartPracticeActivity", "requestAndStart - mode: " + mode + ", topicId: " + topicId + ", numQuestions: " + numQuestions);
        setLoading(true);
        metaSummary.setText("");

        // Try API first with retry
        Log.d("SmartPracticeActivity", "Calling requestRecommendationsWithRetry...");
        client.requestRecommendationsWithRetry(
                this,
                mode,
                topicId,
                numQuestions,
                criticalOnly,
                new AiRecommendationClient.RecommendationCallback() {
                    @Override
                    public void onSuccess(List<String> ids, List<Map<String, Object>> metadata) {
                        Log.d("SmartPracticeActivity", "API success! Got " + (ids != null ? ids.size() : 0) + " recommendations");
                        runOnUiThread(() -> {
                            setLoading(false);
                            if (ids == null || ids.isEmpty()) {
                                Log.d("SmartPracticeActivity", "API returned empty, trying fallback...");
                                // Fallback to heuristic
                                tryFallbackRecommendations(mode, topicId, numQuestions, criticalOnly);
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
                            // Fallback to heuristic when API fails
                            Log.e("SmartPracticeActivity", "API failed, using fallback. Error: " + e.getMessage(), e);
                            tryFallbackRecommendations(mode, topicId, numQuestions, criticalOnly);
                        });
                    }
                }
        );
    }

    private void tryFallbackRecommendations(String mode, Integer topicId, int numQuestions, boolean criticalOnly) {
        Log.d("SmartPracticeActivity", "tryFallbackRecommendations called");
        com.google.firebase.auth.FirebaseUser firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        String userId = firebaseUser != null ? firebaseUser.getUid() : com.example.afinal.analytics.UserIdentity.getUserId(this);
        Log.d("SmartPracticeActivity", "Using fallback for userId: " + userId);

        SmartStudyEngine.getFallbackRecommendations(userId, topicId, numQuestions, criticalOnly, new SmartStudyEngine.FallbackCallback() {
            @Override
            public void onSuccess(List<SmartStudyEngine.QuestionRecommendation> recommendations) {
                runOnUiThread(() -> {
                    setLoading(false);
                    if (recommendations == null || recommendations.isEmpty()) {
                        Toast.makeText(SmartPracticeActivity.this, "Chưa đủ dữ liệu để gợi ý ôn thông minh. Hãy làm thêm vài đề trước nhé.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    List<String> ids = new ArrayList<>();
                    for (SmartStudyEngine.QuestionRecommendation rec : recommendations) {
                        ids.add(rec.questionId);
                    }

                    ArrayList<Question> questions = questionDAO.getQuestionsByIds(ids);
                    if (questions.isEmpty()) {
                        Toast.makeText(SmartPracticeActivity.this, "Không tải được danh sách câu hỏi từ cơ sở dữ liệu.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Convert to metadata format for summary
                    List<Map<String, Object>> metadata = new ArrayList<>();
                    for (SmartStudyEngine.QuestionRecommendation rec : recommendations) {
                        Map<String, Object> meta = new HashMap<>();
                        meta.put("topic_id", rec.topicId);
                        meta.put("is_critical", rec.isCritical);
                        meta.put("predicted_correct_prob", rec.predictedCorrectProb);
                        meta.put("urgency_score", rec.dueTimeMs > 0 ? 0.5 : 0.0);
                        metadata.add(meta);
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
        });
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


