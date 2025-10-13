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
    private int l,r;
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
        setDisplay();
        all=findViewById(R.id.btnLobbyChoseAll);
        all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start.setText(String.valueOf(min));
                end.setText(String.valueOf(max));
            }
        });

        run=findViewById(R.id.btnLobbyStart);
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(id.equals("topic")&&!checklegit_start_end()){
                    Log.d("DEBUG_TAG", "notlegit");
                    Toast.makeText(QuestionActivityLobby.this, "Vui lòng nhập bắt đầu và kết thúc hợp lệ", Toast.LENGTH_SHORT).show();
                }
                else{
                    String s = getChoice();
                    if(s.equals("Xem đáp án sau khi hoàn thành bài thi")){

                        Intent nextIntent = new Intent(QuestionActivityLobby.this,QuestionActivityLast.class);
                        nextIntent.putExtra("start",l);
                        nextIntent.putExtra("end",r);
                        String name=intent.getStringExtra("name");
                        nextIntent.putExtra("name",name);
                        nextIntent.putExtra("id",id);
                        startActivity(nextIntent);


                    }
                    else {
                        Intent nextIntent = new Intent(QuestionActivityLobby.this,QuestionActivityNow.class);
                        nextIntent.putExtra("start",l);
                        nextIntent.putExtra("end",r);
                        String name=intent.getStringExtra("name");
                        nextIntent.putExtra("name",name);
                        nextIntent.putExtra("id",id);
                        startActivity(nextIntent);
                    }
                }
            }
        });
        back=findViewById(R.id.btnLobbyBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private String getChoice() {
        radioGroup=findViewById(R.id.rgLobby);
        int id=radioGroup.getCheckedRadioButtonId();
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
        num=findViewById(R.id.txtLobbyNumofQues);
        name=findViewById(R.id.txtLobbyCategoriesName);
        choice=findViewById(R.id.LayoutLobbyChoice);
        info=findViewById(R.id.txtLobbyinfo);
        intent=getIntent();
        id=intent.getStringExtra("id");

        if(id.equals("topic")){
            num.setText(String.valueOf(intent.getIntExtra("num",1))+" câu");
            name.setText(intent.getStringExtra("name"));
            start=findViewById(R.id.txtLobbyStart);
            end=findViewById(R.id.txtLobbyEnd);
            min=intent.getIntExtra("start",1);
            max=intent.getIntExtra("end",1);
            start.setHint("Tối thiểu: "+String.valueOf(min));
            end.setHint("Tối đa: "+String.valueOf(max));
            choice.setVisibility(View.VISIBLE);
            info.setText("Đầu tiên, nhập câu hỏi bắt đầu và kết thúc mà bạn muốn ôn tập.");
        }
        else{
            choice.setVisibility(View.GONE);
        }

    }
}