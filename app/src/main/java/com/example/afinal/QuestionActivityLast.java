package com.example.afinal;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import java.util.HashMap;

public class QuestionActivityLast extends AppCompatActivity {
    private TextView topicname;
    private ImageButton back;
    private Button submit,next,prev;

    private SQLiteDatabase database= null;
    private TextView content;
    private RadioButton a,b,c,d;
    private ImageView imgQuestion;
    private String ans="",explaination="",img_url="";
    private RadioGroup radioGroup;
    private int anInt=1;
    private HashMap<Integer,String> hashMap;
    private HashMap<Integer,String> answer;
    private long count;
    private int start,end;
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
        Intent intent=getIntent();
        String topic=intent.getStringExtra("name");
        topicname=findViewById(R.id.txtTopicQA);
        topicname.setText(topic);
        backSetup();
        start=intent.getIntExtra("start",1);
        end=intent.getIntExtra("end",1);
        setsql(start,end);
        submitSetup();
    }

    private void setsql(int start,int end) {
        content=findViewById(R.id.txtQAcontent);
        a=findViewById(R.id.radiobtnQAa);
        b=findViewById(R.id.radiobtnQAb);
        c=findViewById(R.id.radiobtnQAc);
        d=findViewById(R.id.radiobtnQAd);
        next=findViewById(R.id.btnnextQA);
        prev=findViewById(R.id.btnprevQA);
        radioGroup=findViewById(R.id.radioBtnQA);
        imgQuestion=findViewById(R.id.imgQuestion);
        hashMap=new HashMap<>();
        answer=new HashMap<>();
        database=openOrCreateDatabase("ATGT3.db",MODE_PRIVATE,null);

        count= DatabaseUtils.queryNumEntries(database,"Questions","question_id BETWEEN ? AND ?",new String[]{String.valueOf(start),String.valueOf(end)});
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull RadioGroup group, int checkedId) {
                if (checkedId != -1) {
                    RadioButton selected = findViewById(checkedId);
                    if (selected != null) {
                        String chosen = selected.getText().toString();
                        hashMap.put(anInt, chosen);
                    }
                }
            }
        });
        Cursor cursor = database.query("Questions",null,"question_id BETWEEN ? AND ?",new String[]{String.valueOf(start),String.valueOf(end)},null,null,null);
        if(cursor.moveToFirst()){
            set_content(cursor);
            answer.put(1,ans);
        }
        else finish();
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cursor.isFirst()) return;
                else{
                    cursor.moveToPrevious();
                    anInt--;
                    set_content(cursor);
                    answer.put(anInt,ans);
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cursor.isLast()) return;
                else{
                    anInt++;
                    cursor.moveToNext();
                    set_content(cursor);
                    answer.put(anInt,ans);
                }
            }
        });

    }

    private void set_content(Cursor cursor) {
        content.setText("Câu "+cursor.getString(0)+": "+cursor.getString(2));
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
                Toast.makeText(QuestionActivityLast.this, "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
            }
        }

        String selected = hashMap.get(anInt);
        if (selected != null) {
            if (selected.equals(a.getText().toString())) a.setChecked(true);
            else if (selected.equals(b.getText().toString())) b.setChecked(true);
            else if (selected.equals(c.getText().toString())) c.setChecked(true);
            else if (selected.equals(d.getText().toString())) d.setChecked(true);
        }
        else radioGroup.clearCheck();
    }


    private void submitSetup() {
        submit=findViewById(R.id.btnQA_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder= new AlertDialog.Builder(QuestionActivityLast.this);
                builder.setTitle("Bạn chắc chắn muốn nộp bài chứ?");
                builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showpoint();
                    }

                    private void showpoint() {
                        AlertDialog.Builder builder1=new AlertDialog.Builder(QuestionActivityLast.this);
                        builder1.setTitle("Kết quả");
                        String msg="/"+count;
                        int truecnt=0;
                        for(int i=1;i<=count;i++){
                            if(hashMap.getOrDefault(i," ").equals(answer.getOrDefault(i,"  " ))){
                                truecnt++;
                            }
                        }
                        msg=String.valueOf(truecnt)+msg;
                        builder1.setMessage(msg);
                        builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                        AlertDialog alertDialog=builder1.create();
                        alertDialog.show();
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

    private void backSetup() {
        back=findViewById(R.id.btnBackQA);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder= new AlertDialog.Builder(QuestionActivityLast.this);
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