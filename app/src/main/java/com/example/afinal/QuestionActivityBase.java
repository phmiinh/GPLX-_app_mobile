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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class QuestionActivityBase extends AppCompatActivity {
    protected  TextView topicname;
    protected  ImageButton back;
    protected  Button submit;

    protected SQLiteDatabase database= null;
    protected  TextView content;
    protected  RadioButton a,b,c,d;
    protected ImageView imgQuestion;
    protected  String ans="",explaination="",img_url="",id,state="Trượt";
    protected  RadioGroup radioGroup;
    protected  ArrayList<Integer> listofquestion=new ArrayList<>();
    protected  HashMap<Integer,String> hashMap;
    protected  HashMap<Integer,String> answer;
    protected  int count;
    protected  int start,end,level,min,time,total,ques_id,critical=0,topicid;
    protected  Intent intent;
    protected  Cursor cursor=null;

    protected  HashMap<Integer,Integer>rule;


    protected void init(){
        intent=getIntent();
        database=openOrCreateDatabase("ATGT.db",MODE_PRIVATE,null);
        get_from_intent();
        hashMap=new HashMap<>();
        answer=new HashMap<>();
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
    protected void setting(Cursor cursor,Context context) {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull RadioGroup group, int checkedId) {
                if (checkedId != -1) {
                    RadioButton selected = findViewById(checkedId);
                    if (selected != null) {
                        String chosen = selected.getText().toString();
                        hashMap.put(ques_id, chosen);
                    }
                }
            }
        });
        if(cursor.moveToFirst()){
            int cnt=1;
            if(id.equals("topic")&&topicid==7){
                while(true){
                    if(cnt==start) break;
                    cnt++;
                    cursor.moveToNext();
                }
            }
        }
        else {
            Log.d("DEBUG_TAG", "Can't find data");
            finish();
        }


    }
    protected void set_content(Cursor cursor,Context context) {
        ques_id=cursor.getInt(0);
        content.setText("Câu "+ques_id+": "+cursor.getString(2));
        a.setText(cursor.getString(6));
        b.setText(cursor.getString(7));
        c.setVisibility(View.VISIBLE);
        d.setVisibility(View.VISIBLE);
        String ansd="",ansc="";
        ansc=cursor.getString(8);
        ansd=cursor.getString(9);
        ans=cursor.getString(10);
        if(ansc==null){
            c.setVisibility(View.GONE);
        }
        else c.setText(ansc);
        if(ansd==null){
            d.setVisibility(View.GONE);
        }
        else d.setText(ansd);
        if(cursor.getInt(4)==1) critical=ques_id;
        explaination=cursor.getString(5);
        img_url=cursor.getString(3);
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
    protected void setCursor() {
        if(id.equals("topic")){
            if(topicid<7){
                cursor = database.query("Questions",null,"question_id BETWEEN ? AND ?",new String[]{String.valueOf(start),String.valueOf(end)},null,null,null);
            }
            else{
                cursor = database.query("Questions",null,"is_critical=?",new String[]{"1"},null,null,"question_id asc",String.valueOf(end));
            }
        }
        else{
            get_rule();
            String sql="SELECT * from(\n" +
                    "\tselect * from(\n" +
                    "\t\tSELECT * from Questions\n" +
                    "\t\twhere is_critical=1\n" +
                    "\t\torder by random()\n" +
                    "\t\tlimit 1\n" +
                    "\t)\n" +
                    "\tunion all\n" +
                    "\tselect * from(\n" +
                    "\t\tSELECT * from Questions\n" +
                    "\t\twhere category_id=1 and is_critical=0\n" +
                    "\t\torder by random()\n" +
                    "\t\tlimit " + rule.getOrDefault(1,1)+
                    "\t)\n" +
                    "\tunion all\n" +
                    "\tselect * from(\n" +
                    "\t\tSELECT * from Questions\n" +
                    "\t\twhere category_id=2 and is_critical=0\n" +
                    "\t\torder by random()\n" +
                    "\t\tlimit " + rule.getOrDefault(2,1)+
                    "\t)\n" +
                    "\tunion all\n" +
                    "\tselect * from(\n" +
                    "\t\tSELECT * from Questions\n" +
                    "\t\twhere category_id=3 and is_critical=0\n" +
                    "\t\torder by random()\n" +
                    "\t\tlimit " + rule.getOrDefault(3,1)+
                    "\t)\n" +
                    "\tunion all\n" +
                    "\tselect * from(\n" +
                    "\t\tSELECT * from Questions\n" +
                    "\t\twhere category_id=4 and is_critical=0\n" +
                    "\t\torder by random()\n" +
                    "\t\tlimit " + rule.getOrDefault(4,1)+
                    "\t)\n" +
                    "\tunion all\n" +
                    "\tselect * from(\n" +
                    "\t\tSELECT * from Questions\n" +
                    "\t\twhere category_id=5 and is_critical=0\n" +
                    "\t\torder by random()\n" +
                    "\t\tlimit " + rule.getOrDefault(5,1)+
                    "\t)\n" +
                    "\tunion all\n" +
                    "\tselect * from(\n" +
                    "\t\tSELECT * from Questions\n" +
                    "\t\twhere category_id=6 and is_critical=0\n" +
                    "\t\torder by random()\n" +
                    "\t\tlimit " + rule.getOrDefault(6,1)+
                    "\t)\n" +
                    "\n" +
                    ")\n" +
                    "order by random()";
            cursor=database.rawQuery(sql,null);
        }
    }

    protected void get_rule() {
        Cursor cursor1=database.query("rules_exam",null,"level_id=?",new String[]{String.valueOf(level)},null,null,null);
        if(cursor1.moveToFirst()){
            while (!cursor1.isAfterLast()){
                rule.put(cursor1.getInt(1),cursor1.getInt(2));
                cursor1.moveToNext();
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
        for(Integer q:hashMap.keySet()){
            if(hashMap.get(q).equals(answer.get(q))) {
                truecnt++;
                if(q==critical){
                    state="Đỗ";
                }
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
                getfullques();
                nextIntent.putExtra("choice",hashMap);
                nextIntent.putExtra("list",listofquestion);
                startActivity(nextIntent);
                finish();
            }
        });
        AlertDialog alertDialog=builder1.create();
        alertDialog.show();
    }
    protected   void getfullques(){
        cursor.moveToFirst();
        if(id.equals("topic")&&topicid==7){
            int cnt=1;
            while(true){
                if(cnt==start) break;
                cnt++;
                cursor.moveToNext();
            }
        }
        while(!cursor.isAfterLast()){
            listofquestion.add(cursor.getInt(0));
            cursor.moveToNext();
        }
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