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
    // THÊM BIẾN NÀY
 //   protected ImageView bookmarkButton;
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
        topicname.setText(topic);
        backSetup(QuestionActivityLast.this);
        setCursor();

        // KIỂM TRA DỮ LIỆU CƠ BẢN TRƯỚC KHI GỌI setting()
        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(this, "Không tìm thấy câu hỏi nào.", Toast.LENGTH_LONG).show();
            finish(); // Đóng Activity và quay về trước đó (MainActivity)
            return; // Dừng hàm onCreate
        }

        setting(cursor,QuestionActivityLast.this);
        // THÊM DÒNG NÀY ĐỂ KÍCH HOẠT LOGIC BOOKMARK
        // Hàm này sẽ thiết lập Listener và tải trạng thái bookmark ban đầu
        setupBookmark(QuestionActivityLast.this);
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
        // THÊM DÒNG NÀY (Đã sửa lại để sử dụng ImageButton/ImageView)
        bookmarkButton = findViewById(R.id.iv_bookmark_button);
    }
    @Override
    protected void setting(Cursor cursor, Context context) {
        super.setting(cursor,context);
        set_content(cursor,context);
        answer.put(ques_id,ans);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cursor.isFirst()) return;
                else{
                    cursor.moveToPrevious();
                    set_content(cursor,context);
                    answer.put(ques_id,ans);
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cursor.isLast()) return;
                else{
                    cursor.moveToNext();
                    set_content(cursor,context);
                    answer.put(ques_id,ans);
                }
            }
        });

    }

    @Override
    protected  void set_content(Cursor cursor, Context context){
        super.set_content(cursor,context);
        String selected = hashMap.get(ques_id);
        if (selected != null) {
            if (selected.equals(a.getText().toString())) a.setChecked(true);
            else if (selected.equals(b.getText().toString())) b.setChecked(true);
            else if (selected.equals(c.getText().toString())) c.setChecked(true);
            else if (selected.equals(d.getText().toString())) d.setChecked(true);
        }
        else {
            radioGroup.clearCheck();
            hashMap.remove(ques_id);
        }
        if (bookmarkButton != null) {
            loadBookmarkState(context);
        }
    }
}