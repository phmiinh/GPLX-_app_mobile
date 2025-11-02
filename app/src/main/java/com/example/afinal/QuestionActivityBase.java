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

import com.example.afinal.DAO.QuestionDAO;
import com.example.afinal.dbclass.Question;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class QuestionActivityBase extends AppCompatActivity {
    protected  TextView topicname,content;
    protected  ImageButton back;
    protected  Button submit;
    protected SQLiteDatabase database= null;
    protected  RadioButton a,b,c,d;
    protected ImageView imgQuestion;
    protected  String  img_url="",id,state="Trượt";
    protected  RadioGroup radioGroup;
    protected  int start,end,level,min,time,topicid,count;
    protected  Intent intent;
    protected  HashMap<Integer,Integer>rule;
    protected ArrayList<Question> listQuestion;
    protected QuestionDAO questionDAO;
    protected int anInt = 0;

    protected void init(){
        intent=getIntent();
        database=openOrCreateDatabase("ATGT.db",MODE_PRIVATE,null);
        get_from_intent();
        questionDAO=new QuestionDAO(database);
        get_list_question();
    }
    private void get_list_question() {
        if(id.equals("topic")){
            if(topicid<7){
                listQuestion=questionDAO.getQuestionInRange(start,end);
            }
            else{
                listQuestion=questionDAO.getCriticalQuestionInRange(start,end);
            }
        }
        else {
            listQuestion=questionDAO.getQuestionOfLevel(level);
        }

    }
    protected void get_from_intent() {
        id=intent.getStringExtra("id");
        if(id.equals("topic")){
            start=intent.getIntExtra("start",1);
            end=intent.getIntExtra("end",1);
            count= end-start+1;
            topicid=intent.getIntExtra("categories_id",0);
            Log.d("con cac", "topicid: "+topicid);
        }
        else {
            level = intent.getIntExtra("level_id", 1);
            min = intent.getIntExtra("min", 1);
            count = intent.getIntExtra("total", 1);
            Log.d("TAG", "onClick: " + count);
            time = intent.getIntExtra("time", 1);
            rule = new HashMap<>();
        }
    }
    protected  void setting(Context context){
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull RadioGroup group, int checkedId) {
                if (checkedId != -1) {
                    RadioButton selected = findViewById(checkedId);
                    if (selected != null) {
                        String chosen = selected.getText().toString();
                        listQuestion.get(anInt).setUserChoice(chosen);
                    }
                }
            }
        });
        if(listQuestion.isEmpty()){
            Log.d("DEBUG_TAG", "Can't find data");
            finish();
        }
    }
    protected void set_content(Question question,Context context) {
        if(id.equals("topic")&&topicid<7) content.setText("Câu "+question.getId()+": "+question.getContent());
        else content.setText("Câu "+String.valueOf(anInt+1)+": "+question.getContent());
        a.setText(question.getA());
        b.setText(question.getB());
        c.setVisibility(View.VISIBLE);
        d.setVisibility(View.VISIBLE);
        if(question.getC()==null){
            c.setVisibility(View.GONE);
        }
        else c.setText(question.getC());
        if(question.getD()==null){
            d.setVisibility(View.GONE);
        }
        else d.setText(question.getD());
        img_url=question.getImg_url();
        imgQuestion.setVisibility(View.VISIBLE);
        if(img_url==null){
            imgQuestion.setVisibility(View.GONE);
        }
        else{
            img_url="img/"+img_url+".png";
            try{
                InputStream inputStream=getAssets().open(img_url);
                Drawable drawable=Drawable.createFromStream(inputStream,null);
                inputStream.close();
                imgQuestion.setImageDrawable(drawable);
            }
            catch (IOException e){
                Toast.makeText(context, "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        database.close();
    }
    protected void submitSetup(Context context) {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder= new AlertDialog.Builder(context);
                builder.setTitle("Bạn chắc chắn muốn nộp bài chứ?");
                builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showpoint(context);
                    }
                });
                builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog=builder.create();
                alertDialog.show();
            }
        });
    }
    protected void showpoint(Context context) {
        AlertDialog.Builder builder1=new AlertDialog.Builder(context);
        builder1.setTitle("Kết quả");
        String msg="/"+count;
        int truecnt=0;
        for(Question question:listQuestion){
            if(question.getUserChoice()==null) continue;
            if(question.getAnswer().equals(question.getUserChoice())){
                truecnt++;
                if(question.getIs_critical()==1) state="Đỗ";
            }
        }
        msg=String.valueOf(truecnt)+msg;
        if(id.equals("level")){
            msg+="\n";
            msg+="Trạng thái: ";
            if(truecnt<min) state="Trượt";
            msg+=state;
        }
        builder1.setMessage(msg);
        builder1.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder1.setPositiveButton("Xem lại bài làm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent nextIntent=new Intent(context,QuestionActivityReview.class);
                nextIntent.putParcelableArrayListExtra("listQuestion",listQuestion);
                startActivity(nextIntent);
                finish();
            }
        });
        AlertDialog alertDialog=builder1.create();
        alertDialog.show();
    }

    protected void backSetup(Context context) {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder= new AlertDialog.Builder(context);
                builder.setTitle("Bạn chắc chắn muốn thoát chứ?");
                builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                });
                builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog=builder.create();
                alertDialog.show();
            }
        });

    }
}