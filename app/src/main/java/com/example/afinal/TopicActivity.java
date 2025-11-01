package com.example.afinal;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.afinal.adapter.CategoriesAdapter;
import com.example.afinal.dbclass.Categories;
import com.example.afinal.dbclass.Level;

import java.util.ArrayList;

public class TopicActivity extends AppCompatActivity {
    private ArrayList<Level> arrayList;
    private ListView lvTopic;
    private ArrayList<Categories> list;
    private SQLiteDatabase database=null;
    private Button btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_topic);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        database = openOrCreateDatabase("ATGT.db",MODE_PRIVATE,null);
        findView();
        tab_topic_setup();
    }
    private void tab_topic_setup() {
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        list=new ArrayList<>();
        Cursor cursor = database.query("categories",null,null,null,null,null,null);
        if(cursor.moveToFirst()) {
            while (!cursor.isAfterLast()){
                list.add(new Categories(cursor.getInt(0),
                        cursor.getString(1),cursor.getInt(2),cursor.getInt(3),cursor.getInt(4)));
                cursor.moveToNext();
            }

        }
        list.add(new Categories(7,"Câu hỏi điểm liệt",60,1,60));
        //View tabView = findViewById(R.id.tab_topic_main);

        CategoriesAdapter adapter=new CategoriesAdapter(TopicActivity.this,R.layout.layout_listview_topic,list);
        lvTopic.setAdapter(adapter);
        lvTopic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(TopicActivity.this, QuestionActivityLobby.class);
                Categories categories=list.get(position);
                intent.putExtra("categories_id",categories.getId());
                intent.putExtra("name",categories.getName());
                intent.putExtra("num",categories.getNum());
                intent.putExtra("start",categories.getStart());
                intent.putExtra("end",categories.getEnd());
                intent.putExtra("id","topic");
                startActivity(intent);
            }
        });
    }
    private void findView() {
        lvTopic = findViewById(R.id.lv_topic);
        btnBack=findViewById(R.id.btnBackTopic);
    }
}