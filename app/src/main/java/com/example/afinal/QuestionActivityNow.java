package com.example.afinal;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.afinal.dbclass.Question;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
public class QuestionActivityNow extends QuestionActivityBase {

    private TextView explain;
    private Button btnAIExplain;
    private TextView txtAIExplain;
    private ProgressBar progressAI;
    private View explanationSection;
    private View aiExplanationSection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_question_now);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        String topic=intent.getStringExtra("name");
        if(id.equals("topic")) {
            topicname.setText(topic);
        } else {
            topicname.setText("Hạng " + topic);
        }
        backSetup(QuestionActivityNow.this);
        setting(QuestionActivityNow.this);
        submitSetup(QuestionActivityNow.this);
        settingtimer(QuestionActivityNow.this);
    }
    @Override
    protected void init(){
        super.init();
        // If SmartPracticeActivity provided a prepared question list, reuse it
        ArrayList<Question> provided = QuestionHolder.consumeQuestions();
        if (provided != null && !provided.isEmpty()) {
            listQuestion = provided;
            count = listQuestion.size();
            start = 1;
        }
        find_view();
    }
    private void find_view() {
        topicname=findViewById(R.id.txtTopicQAN);
        content=findViewById(R.id.txtQANcontent);
        a=findViewById(R.id.radiobtnQANa);
        b=findViewById(R.id.radiobtnQANb);
        c=findViewById(R.id.radiobtnQANc);
        d=findViewById(R.id.radiobtnQANd);
        next=findViewById(R.id.btnnextQAN);
        radioGroup=findViewById(R.id.radioBtnQAN);
        imgQuestion=findViewById(R.id.imgQAN);
        explain=findViewById(R.id.txtQANexplain);
        submit=findViewById(R.id.btnQAN_submit);
        back=findViewById(R.id.btnBackQAN);
        bookmarkButton=findViewById(R.id.iv_bookmark_button);
        timer=findViewById(R.id.txtQANtimer);
        setupBookmarkButton();
        
        // New views for redesigned layout
        explanationSection = findViewById(R.id.explanation_section);
        aiExplanationSection = findViewById(R.id.ai_explanation_section);
        txtAIExplain = findViewById(R.id.txtAIExplain);
        progressAI = findViewById(R.id.progressAI);
        btnAIExplain=findViewById(R.id.btnAIExplain);
        
        if (btnAIExplain != null) {
            btnAIExplain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestAiExplanationForCurrent();
                }
            });
        }
    }
    @Override
    protected void setting(Context context) {
        super.setting(context);
        set_content(listQuestion.get(0), context);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                if (selectedId == -1){
                    Toast.makeText(QuestionActivityNow.this, "Vui lòng chọn đáp án trước khi kiểm tra.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(next.getText().toString().equals("Kiểm tra")){
                    // Show standard explanation
                    explain.setText(listQuestion.get(anInt).getExplain());
                    if (explanationSection != null) {
                        explanationSection.setVisibility(View.VISIBLE);
                    }
                    
                    // Show AI explanation section
                    if (aiExplanationSection != null) {
                        aiExplanationSection.setVisibility(View.VISIBLE);
                    }
                    if (btnAIExplain != null) {
                        btnAIExplain.setVisibility(View.VISIBLE);  // Đảm bảo button hiện lại
                        btnAIExplain.setEnabled(true);
                        btnAIExplain.setText("Tạo giải thích");
                    }
                    if (txtAIExplain != null) {
                        txtAIExplain.setText("Nhấn nút \"Tạo giải thích\" bên dưới để AI phân tích chi tiết câu hỏi này.");
                    }
                    // Reset AI usage flag for new question check
                    aiUsedForCurrentQuestion = false;
                    
                    next.setText("Câu tiếp theo");
                    // Highlight correct (green) and incorrect (red) answers
                    AnswerColorHelper.showAnswerWithColors(a,b,c,d, radioGroup, listQuestion.get(anInt).getAnswer());
                    return;
                }
                anInt++;
                if(anInt>=listQuestion.size()) {
                    return;
                }
                else{
                    explain.setText("");
                    next.setText("Kiểm tra");
                    set_content(listQuestion.get(anInt),context);
                    
                    // Hide explanation sections for next question
                    if (explanationSection != null) {
                        explanationSection.setVisibility(View.GONE);
                    }
                    if (aiExplanationSection != null) {
                        aiExplanationSection.setVisibility(View.GONE);
                    }
                    // Reset AI explanation button state for next question
                    if (btnAIExplain != null) {
                        btnAIExplain.setVisibility(View.VISIBLE);  // Đảm bảo button sẵn sàng cho câu tiếp theo
                        btnAIExplain.setEnabled(false);  // Disable cho đến khi bấm "Kiểm tra"
                    }
                    if (txtAIExplain != null) {
                        txtAIExplain.setText("");
                    }
                    if (progressAI != null) {
                        progressAI.setVisibility(View.GONE);
                    }
                }
            }
        });

    }
    @Override
    protected void set_content(Question question, Context context) {
        super.set_content(question,context);
        radioGroup.clearCheck();
        question.setUserChoice(null);
        listQuestion.get(anInt).setUserChoice(null);
        AnswerColorHelper.resetAnswerColors(a,b,c,d);
        
        // Hide all explanation sections for new question
        if (explanationSection != null) {
            explanationSection.setVisibility(View.GONE);
        }
        if (aiExplanationSection != null) {
            aiExplanationSection.setVisibility(View.GONE);
        }
    }

    private void requestAiExplanationForCurrent() {
        if (btnAIExplain == null) return;
        
        // Disable button and show progress
        btnAIExplain.setEnabled(false);
        btnAIExplain.setVisibility(View.GONE);
        if (progressAI != null) {
            progressAI.setVisibility(View.VISIBLE);
        }
        if (txtAIExplain != null) {
            txtAIExplain.setText("Đang tạo giải thích...");
        }

        final String questionText = content.getText().toString();
        final String optionA = a.getText().toString();
        final String optionB = b.getText().toString();
        final String optionC = c.getVisibility() == View.VISIBLE ? c.getText().toString() : null;
        final String optionD = d.getVisibility() == View.VISIBLE ? d.getText().toString() : null;
        final boolean hasImg = img_url != null && !img_url.isEmpty();
        
        // Get GEMINI_API_KEY from BuildConfig
        String apiKey = BuildConfig.GEMINI_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressAI != null) progressAI.setVisibility(View.GONE);
                    if (btnAIExplain != null) {
                        btnAIExplain.setVisibility(View.VISIBLE);
                        btnAIExplain.setEnabled(true);
                    }
                    if (txtAIExplain != null) {
                        txtAIExplain.setText("API key chưa được cấu hình. Vui lòng thêm GEMINI_API_KEY vào local.properties.");
                    }
                }
            });
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StringBuilder prompt = new StringBuilder();
                    prompt.append("You are an expert Vietnamese driving-license instructor. ");
                    prompt.append("Explain the following multiple-choice question in SHORT, clear Vietnamese. ");
                    prompt.append("First, restate the question very briefly. ");
                    prompt.append("Then explain why the correct option is correct and why the other options are wrong. ");
                    prompt.append("Focus on key rules and exam tips. Answer only in Vietnamese.\n\n");
                    prompt.append(questionText).append("\n");
                    prompt.append("A. ").append(optionA).append("\n");
                    prompt.append("B. ").append(optionB).append("\n");
                    if (optionC != null) prompt.append("C. ").append(optionC).append("\n");
                    if (optionD != null) prompt.append("D. ").append(optionD).append("\n");

                    GeminiApiManager api = new GeminiApiManager();
                    String aiText;
                    if (hasImg) {
                        String assetPath = img_url; // already "img/<file>.png"
                        byte[] bytes;
                        try (InputStream is = getAssets().open(assetPath)) {
                            bytes = new byte[is.available()];
                            int read = is.read(bytes);
                        }
                        aiText = api.generateExplanationWithImage(prompt.toString(), bytes, "image/png");
                    } else {
                        aiText = api.generateExplanation(prompt.toString());
                    }

                    final String result = aiText == null ? "" : aiText.trim();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (progressAI != null) progressAI.setVisibility(View.GONE);
                            
                            if (!result.isEmpty()) {
                                if (txtAIExplain != null) {
                                    txtAIExplain.setText(result);
                                }
                                // Mark that AI was used for this question
                                aiUsedForCurrentQuestion = true;
                                
                                // Hide the button after successful generation
                                if (btnAIExplain != null) {
                                    btnAIExplain.setVisibility(View.GONE);
                                }
                            } else {
                                if (txtAIExplain != null) {
                                    txtAIExplain.setText("AI chưa trả về nội dung. Vui lòng thử lại.");
                                }
                                if (btnAIExplain != null) {
                                    btnAIExplain.setVisibility(View.VISIBLE);
                                    btnAIExplain.setEnabled(true);
                                }
                            }
                        }
                    });
                } catch (IOException | JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (progressAI != null) progressAI.setVisibility(View.GONE);
                            
                            String errorMsg = "Lỗi kết nối API. ";
                            if (e.getMessage() != null && e.getMessage().contains("403")) {
                                errorMsg = "API key không hợp lệ hoặc đã hết hạn. Vui lòng cập nhật key mới trong local.properties.";
                            } else if (e.getMessage() != null) {
                                errorMsg += e.getMessage();
                            }
                            
                            if (txtAIExplain != null) {
                                txtAIExplain.setText(errorMsg);
                            }
                            if (btnAIExplain != null) {
                                btnAIExplain.setVisibility(View.VISIBLE);
                                btnAIExplain.setEnabled(true);
                            }
                            Toast.makeText(QuestionActivityNow.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }
}
