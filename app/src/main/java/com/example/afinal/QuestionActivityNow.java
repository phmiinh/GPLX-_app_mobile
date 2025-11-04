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
    private Button next;
    private TextView explain;
    private Button btnAIExplain;
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
        if(id.equals("topic")) topicname.setText(topic);
        else topicname.setText("Hạng "+topic);
        backSetup(QuestionActivityNow.this);
        setting(QuestionActivityNow.this);
        submitSetup(QuestionActivityNow.this);
    }
    @Override
    protected void init(){
        super.init();
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
        setupBookmarkButton();
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
                int id=radioGroup.getCheckedRadioButtonId();
                if(id==-1){
                    Toast.makeText(QuestionActivityNow.this, "Hãy chọn đáp án trước", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(next.getText().toString().equals("Kiểm tra")){
                    explain.setText("Giải thích: "+listQuestion.get(anInt).getExplain());
                    // Log attempt before showing answer
                    logAttemptForCurrent();
                    // Highlight correct (green) and incorrect (red) answers
                    if (btnAIExplain != null) {
                        btnAIExplain.setVisibility(View.VISIBLE);
                        btnAIExplain.setEnabled(true);
                        btnAIExplain.setText("AI giải thích");
                    }
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
                    answer.put(ques_id,ans);
                    if (btnAIExplain != null) {
                        btnAIExplain.setVisibility(View.GONE);
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
        if (btnAIExplain != null) {
            btnAIExplain.setVisibility(View.GONE);
        }
    }

    private void requestAiExplanationForCurrent() {
        if (btnAIExplain == null) return;
        btnAIExplain.setEnabled(false);
        btnAIExplain.setText("Đang giải thích...");

        final String questionText = content.getText().toString();
        final String optionA = a.getText().toString();
        final String optionB = b.getText().toString();
        final String optionC = c.getVisibility() == View.VISIBLE ? c.getText().toString() : null;
        final String optionD = d.getVisibility() == View.VISIBLE ? d.getText().toString() : null;
        final boolean hasImg = img_url != null && !img_url.isEmpty();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StringBuilder prompt = new StringBuilder();
                    prompt.append("Giải thích đáp án chi tiết bằng tiếng Việt, ngắn gọn, dễ hiểu.\n");
                    prompt.append(questionText).append("\n");
                    prompt.append(optionA).append("\n");
                    prompt.append(optionB).append("\n");
                    if (optionC != null) prompt.append(optionC).append("\n");
                    if (optionD != null) prompt.append(optionD).append("\n");
                    prompt.append("Chỉ ra vì sao đáp án đúng là đúng, và vì sao các đáp án khác sai.");

                    GeminiApiManager api = new GeminiApiManager();
                    String aiText;
                    if (hasImg) {
                        // Load image bytes from assets
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
                            if (!result.isEmpty()) {
                                String prev = explain.getText() == null ? "" : explain.getText().toString();
                                String merged = prev.isEmpty() ? ("AI: " + result) : (prev + "\n\nAI: " + result);
                                explain.setText(merged);
                            } else {
                                Toast.makeText(QuestionActivityNow.this, "AI không trả về nội dung.", Toast.LENGTH_SHORT).show();
                            }
                            btnAIExplain.setEnabled(true);
                            btnAIExplain.setText("AI giải thích");
                        }
                    });
                } catch (IOException | JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(QuestionActivityNow.this, "Lỗi gọi AI: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            btnAIExplain.setEnabled(true);
                            btnAIExplain.setText("AI giải thích");
                        }
                    });
                }
            }
        }).start();
    }
}
