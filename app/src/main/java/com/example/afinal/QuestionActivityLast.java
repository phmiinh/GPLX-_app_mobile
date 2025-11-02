package com.example.afinal;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class QuestionActivityLast extends QuestionActivityBase {
    private Button next,prev;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_question_last);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        String topic=intent.getStringExtra("name");
        if(id.equals("topic")) topicname.setText(topic);
        else topicname.setText("Hạng "+topic);
        backSetup(QuestionActivityLast.this);
        setting(QuestionActivityLast.this);
        submitSetup(QuestionActivityLast.this);
    }
    @Override
    protected void init(){
        super.init();
        find_view();
    }
    private void find_view() {
        topicname=findViewById(R.id.txtTopicQAL);
        submit=findViewById(R.id.btnQAL_submit);
        back=findViewById(R.id.btnBackQAL);
        content=findViewById(R.id.txtQALcontent);
        a=findViewById(R.id.radiobtnQALa);
        b=findViewById(R.id.radiobtnQALb);
        c=findViewById(R.id.radiobtnQALc);
        d=findViewById(R.id.radiobtnQALd);
        next=findViewById(R.id.btnnextQAL);
        prev=findViewById(R.id.btnprevQAL);
        radioGroup=findViewById(R.id.radioBtnQAL);
        imgQuestion=findViewById(R.id.imgQAL);
    }
    @Override
    protected void setting(Context context) {
        super.setting(context);
        set_content(listQuestion.get(0), context);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anInt--;
                if(anInt<0) {
                    anInt++;
                    return;
                }
                else{
                    set_content(listQuestion.get(anInt),context);
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anInt++;
                if(anInt>=listQuestion.size()) {
                    anInt--;
                    return;
                }
                else{
                    set_content(listQuestion.get(anInt),context);
                }
            }
        });

    }

    @Override
    protected  void set_content(Question question, Context context){
        super.set_content(question,context);
        String selected = question.getUserChoice();
        if (selected != null) {
            if (selected.equals(a.getText().toString())) a.setChecked(true);
            else if (selected.equals(b.getText().toString())) b.setChecked(true);
            else if (selected.equals(c.getText().toString())) c.setChecked(true);
            else if (selected.equals(d.getText().toString())) d.setChecked(true);
        }
        else {
            radioGroup.clearCheck();
            question.setUserChoice(null);
            listQuestion.get(anInt).setUserChoice(null);
        }
    }
}