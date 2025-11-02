package com.example.afinal;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.afinal.adapter.QuestionAdapter;
import com.example.afinal.dbclass.Question;

import java.util.ArrayList;
import java.util.HashMap;

public class QuestionActivityReview extends AppCompatActivity {
    private Button back;
    private ListView lv;
    private TextView info;
    private  ArrayList<Question> listQuestion;
    private HashMap<Integer,String> choice;
    private  Intent intent;
    private String result,startTime,endTime,time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_question_review);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        lvsetting();
        backsetup();
        setInfo();

    }
    private void setInfo() {
        String s = "Kết quả: "+result+"\n"+"Từ: "+startTime+" đến "+endTime;
        info.setText(s);
    }
    private  void init(){
        intent=getIntent();
        find_view();
        get_from_intent();
    }

    private void get_from_intent() {
        listQuestion=intent.getParcelableArrayListExtra("listQuestion");
        result=intent.getStringExtra("result");
        startTime=intent.getStringExtra("startTime");
        endTime=intent.getStringExtra("endTime");
        time=intent.getStringExtra("time");
    }

    private void find_view() {
        lv=findViewById(R.id.lvQAR);
        info=findViewById(R.id.txtQARinfo);
    }

    private void lvsetting() {
        QuestionAdapter adapter=new QuestionAdapter(QuestionActivityReview.this,R.layout.layout_listview_review,listQuestion);
        lv.setAdapter(adapter);
    }
    private void backsetup() {
        back=findViewById(R.id.btnQARback);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}