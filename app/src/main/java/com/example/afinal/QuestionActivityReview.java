package com.example.afinal;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

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
    private ArrayList<Integer> list_question;
    private HashMap<Integer,String> choice;
    private SQLiteDatabase database=null;
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
        database=openOrCreateDatabase("ATGT.db",MODE_PRIVATE,null);
        Intent intent=getIntent();
        list_question=intent.getIntegerArrayListExtra("list");
        choice=(HashMap<Integer,String>) intent.getSerializableExtra("choice");
        lvsetting();
        backsetup();

    }

    private void lvsetting() {
        lv=findViewById(R.id.lvQAR);

        ArrayList<Question>list=new ArrayList<>();
        getList(list);
        QuestionAdapter adapter=new QuestionAdapter(QuestionActivityReview.this,R.layout.layout_listview_review,list);
        lv.setAdapter(adapter);

    }

    private void getList(ArrayList<Question> list) {
        for(Integer i:list_question){
            Cursor cursor=database.query("questions",null,"question_id=?",new String[]{String.valueOf(i)},null,null,null);
            cursor.moveToFirst();
            Question x=new Question();
            x.setId(cursor.getInt(0));
            x.setContent(cursor.getString(2));
            x.setImg_url(cursor.getString(3));
            x.setExplain(cursor.getString(5));
            x.setA(cursor.getString(6));
            x.setB(cursor.getString(7));
            x.setC(cursor.getString(8));
            x.setD(cursor.getString(9));
            x.setAnswer(cursor.getString(10));
            x.setUserChoice(choice.getOrDefault(i,"0"));
            list.add(x);
        }
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