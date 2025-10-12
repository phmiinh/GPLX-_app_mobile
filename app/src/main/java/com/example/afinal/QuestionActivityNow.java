package com.example.afinal;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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

public class QuestionActivityNow extends AppCompatActivity {
    private TextView topicname;
    private ImageButton back;
    private Button submit,next;

    private SQLiteDatabase database= null;
    private TextView content,explain,showans;
    private RadioButton a,b,c,d;
    private ImageView imgQuestion;
    private String ans="",explaination="",img_url="",id;
    private RadioGroup radioGroup;
    private int anInt=1;
    private HashMap<Integer,String> hashMap;
    private HashMap<Integer,String> answer;
    private int count;
    private int start,end;
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
        Intent intent=getIntent();
        String topic=intent.getStringExtra("name");
        topicname=findViewById(R.id.txtTopicQAN);
        topicname.setText(topic);
        database=openOrCreateDatabase("ATGT.db",MODE_PRIVATE,null);
        backSetup();
        id=intent.getStringExtra("id");
        Cursor cursor=null;
        if(id.equals("topic")){
            start=intent.getIntExtra("start",1);
            end=intent.getIntExtra("end",1);
            count= end-start+1;
            cursor = database.query("Questions",null,"question_id BETWEEN ? AND ?",new String[]{String.valueOf(start),String.valueOf(end)},null,null,null);

        }

        setting(cursor);
        submitSetup();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
    }

    private void setting(Cursor cursor) {
        content=findViewById(R.id.txtQANcontent);
        a=findViewById(R.id.radiobtnQANa);
        b=findViewById(R.id.radiobtnQANb);
        c=findViewById(R.id.radiobtnQANc);
        d=findViewById(R.id.radiobtnQANd);
        next=findViewById(R.id.btnnextQAN);
        radioGroup=findViewById(R.id.radioBtnQAN);
        imgQuestion=findViewById(R.id.imgQAN);
        hashMap=new HashMap<>();
        answer=new HashMap<>();
        explain=findViewById(R.id.txtQANexplain);
        showans=findViewById(R.id.txtQANans);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull RadioGroup group, int checkedId) {
                if(!next.getText().toString().equals("Kiểm tra")) return;
                if (checkedId != -1) {
                    RadioButton selected = findViewById(checkedId);
                    if (selected != null) {
                        String chosen = selected.getText().toString();
                        hashMap.put(anInt, chosen);
                    }
                }
            }
        });
        if(cursor.moveToFirst()){
            set_content(cursor);
            answer.put(1,ans);
        }
        else finish();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id=radioGroup.getCheckedRadioButtonId();
                if(id==-1){
                    Toast.makeText(QuestionActivityNow.this, "Hãy chọn đáp án trước", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(next.getText().toString().equals("Kiểm tra")){
                    showans.setText("Đáp án đúng là: "+ans);
                    explain.setText("Giải thích: "+explaination);
                    next.setText("Câu tiếp theo");
                    return;
                }
                if(cursor.isLast()) return;
                else{
                    showans.setText("");
                    explain.setText("");
                    next.setText("Kiểm tra");
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
                Toast.makeText(QuestionActivityNow.this, "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
            }
        }
        radioGroup.clearCheck();
    }


    private void submitSetup() {
        submit=findViewById(R.id.btnQAN_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder= new AlertDialog.Builder(QuestionActivityNow.this);
                builder.setTitle("Bạn chắc chắn muốn nộp bài chứ?");
                builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showpoint();
                    }

                    private void showpoint() {
                        AlertDialog.Builder builder1=new AlertDialog.Builder(QuestionActivityNow.this);
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
        back=findViewById(R.id.btnBackQAN);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder= new AlertDialog.Builder(QuestionActivityNow.this);
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