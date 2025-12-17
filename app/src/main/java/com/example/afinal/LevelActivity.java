package com.example.afinal;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;

import com.example.afinal.adapter.LevelRecyclerAdapter;
import com.example.afinal.dbclass.Level;

import java.util.ArrayList;

public class LevelActivity extends BaseNavigationActivity {
    private RecyclerView rvLevels;
    private ArrayList<Level> arrayList;
    private SQLiteDatabase database = null;
    private LevelRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        // Setup navigation
        setupToolbar(false, "Thi thử");
        enableBottomBar(2); // Mock tab

        // Initialize database
        database = openOrCreateDatabase("ATGT.db", MODE_PRIVATE, null);

        // Initialize views
        rvLevels = findViewById(R.id.rv_levels);

        // Load levels
        loadLevels();
    }

    private void loadLevels() {
        arrayList = new ArrayList<>();
        Cursor cursor = database.query("level", null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                arrayList.add(new Level(
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

        // Setup adapter
        adapter = new LevelRecyclerAdapter(this, arrayList, new LevelRecyclerAdapter.OnLevelClickListener() {
            @Override
            public void onLevelClick(Level level) {
                Intent intent = new Intent(LevelActivity.this, QuestionActivityLobby.class);
                intent.putExtra("id", "level");
                intent.putExtra("level_id", level.getLevel_id());
                intent.putExtra("min", level.getMinRequired());
                intent.putExtra("total", level.getTotalQuestion());
                intent.putExtra("name", level.getName());
                intent.putExtra("time", level.getTime());
                startActivity(intent);
            }
        });
        rvLevels.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        if (database != null && database.isOpen()) {
            database.close();
        }
        super.onDestroy();
    }
}