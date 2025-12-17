package com.example.afinal;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;

import com.example.afinal.adapter.TopicAdapter;
import com.example.afinal.dbclass.Categories;

import java.util.ArrayList;

public class TopicActivity extends BaseNavigationActivity {
    private RecyclerView rvTopics;
    private ArrayList<Categories> list;
    private SQLiteDatabase database = null;
    private TopicAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        // Setup navigation
        setupToolbar(false, "Ôn theo chủ đề");
        enableBottomBar(1); // Practice tab

        // Initialize database
        database = openOrCreateDatabase("ATGT.db", MODE_PRIVATE, null);

        // Initialize views
        rvTopics = findViewById(R.id.rv_topics);

        // Load topics
        loadTopics();
    }

    private void loadTopics() {
        list = new ArrayList<>();
        Cursor cursor = database.query("categories", null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                list.add(new Categories(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getInt(3),
                        cursor.getInt(4)
                ));
                cursor.moveToNext();
            }
            cursor.close();
        }

        // Add "Điểm liệt" category
        list.add(new Categories(7, "Câu hỏi điểm liệt", 60, 1, 60));

        // Setup adapter
        adapter = new TopicAdapter(this, list, new TopicAdapter.OnTopicClickListener() {
            @Override
            public void onTopicClick(Categories category) {
                Intent intent = new Intent(TopicActivity.this, QuestionActivityLobby.class);
                intent.putExtra("categories_id", category.getId());
                intent.putExtra("name", category.getName());
                intent.putExtra("num", category.getNum());
                intent.putExtra("start", category.getStart());
                intent.putExtra("end", category.getEnd());
                intent.putExtra("id", "topic");
                startActivity(intent);
            }
        });
        rvTopics.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        if (database != null && database.isOpen()) {
            database.close();
        }
        super.onDestroy();
    }
}