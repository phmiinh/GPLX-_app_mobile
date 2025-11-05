package com.example.afinal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class QuestionActivityLobby extends AppCompatActivity {
    private TextView name,num,info;
    private EditText start,end;
    private ImageButton back;
    private Button run,all;
    private int min,max;
    private RadioGroup radioGroup;
    private Intent intent;
    private int l,r,total,level_id,time,topicid;
    private String id;
    private LinearLayout choice;
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_question_lobby);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        setDisplay();
        choose_all_setting();
        run_setting();
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void init() {
        find_view();
        intent=getIntent();
        get_from_intent();
    }

    private void get_from_intent() {
        id=intent.getStringExtra("id");
        topicid=intent.getIntExtra("categories_id",0);
        Log.d("con cac", "topicidlobby: "+topicid);
        if(id.equals("topic")){
            min=intent.getIntExtra("start",1);
            max=intent.getIntExtra("end",1);
        }
        else{
            min=intent.getIntExtra("min",1);
            time=intent.getIntExtra("time",1);
            total=intent.getIntExtra("total",1);
            level_id=intent.getIntExtra("level_id",1);
        }
    }

    private void find_view() {
        all=findViewById(R.id.btnLobbyChoseAll);
        run=findViewById(R.id.btnLobbyStart);
        back=findViewById(R.id.btnLobbyBack);
        radioGroup=findViewById(R.id.rgLobby);
        num=findViewById(R.id.txtLobbyNumofQues);
        name=findViewById(R.id.txtLobbyCategoriesName);
        choice=findViewById(R.id.LayoutLobbyChoice);
        info=findViewById(R.id.txtLobbyinfo);
        start=findViewById(R.id.txtLobbyStart);
        end=findViewById(R.id.txtLobbyEnd);
    }

    private void choose_all_setting() {
        all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start.setText(String.valueOf(min));
                end.setText(String.valueOf(max));
            }
        });
    }

    private void run_setting() {
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(id.equals("topic")&&!checklegit_start_end()){
                    Log.d("DEBUG_TAG", "notlegit");
                    Toast.makeText(QuestionActivityLobby.this, "Vui lòng nhập bắt đầu và kết thúc hợp lệ", Toast.LENGTH_SHORT).show();
                }
                else{
                    String s = getChoice();
                    if(s.equals("")) {
                        Toast.makeText(QuestionActivityLobby.this, "Vui lòng chọn hình thức!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent nextIntent;
                    if(s.equals("Xem đáp án sau khi hoàn thành bài thi")){
                        nextIntent = new Intent(QuestionActivityLobby.this,QuestionActivityLast.class);
                    }
                    else {
                        nextIntent = new Intent(QuestionActivityLobby.this,QuestionActivityNow.class);
                    }
                    String name=intent.getStringExtra("name");
                    nextIntent.putExtra("name",name);
                    nextIntent.putExtra("id",id);
                    nextIntent.putExtra("categories_id",topicid);
                    if(id.equals("topic")){
                        nextIntent.putExtra("start",l);
                        nextIntent.putExtra("end",r);
                    }
                    else{
                        nextIntent.putExtra("level_id",level_id);
                        nextIntent.putExtra("min",min);

                        Log.d("TAG", "onClick: "+total);
                        nextIntent.putExtra("total",total);
                        nextIntent.putExtra("time",time);
                    }
                    startActivity(nextIntent);
                }
            }
        });
    }


    private String getChoice() {
        int id=radioGroup.getCheckedRadioButtonId();
        if(id==-1) return "";
        RadioButton radioButton=findViewById(id);
        return radioButton.getText().toString();
    }

    private boolean checklegit_start_end() {
        if (start.getText().toString().equals("") || end.getText().toString().equals("")) return false;
        l = Integer.parseInt(start.getText().toString());
        r = Integer.parseInt(end.getText().toString());
        if (l > r || l < min || r > max) return false;
        else return true;
    }

    private void setDisplay() {
        if(id.equals("topic")){
            name.setText(intent.getStringExtra("name"));
            num.setText(String.valueOf(intent.getIntExtra("num",1))+" câu");
            start.setHint("Tối thiểu: "+String.valueOf(min));
            end.setHint("Tối đa: "+String.valueOf(max));
            choice.setVisibility(View.VISIBLE);
            info.setText("Đầu tiên, nhập câu hỏi bắt đầu và kết thúc mà bạn muốn ôn tập.");
        }
        else{
            name.setText("Hạng "+intent.getStringExtra("name"));
            choice.setVisibility(View.GONE);
            num.setText(String.valueOf(total)+" câu");
            info.setText("Cần đúng ít nhất: "+min+"\n"+"Thời gian: "+time+" phút.");
        }
    }
}