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
import android.widget.TabHost;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.afinal.adapter.LevelAdapter;
import com.example.afinal.dbclass.Categories;
import com.example.afinal.dbclass.Level;

import java.util.ArrayList;

public class LevelActivity extends AppCompatActivity {
    private ArrayList<Level> arrayList;
    private ListView lvLevel;
    private SQLiteDatabase database=null;
    private Button btnBackLevel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_level);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        database = openOrCreateDatabase("ATGT.db",MODE_PRIVATE,null);
        findView();
        tab_level_setup();

    }
    private void findView() {
        lvLevel=findViewById(R.id.lvLevel);
        btnBackLevel=findViewById(R.id.btnBackLevel);
    }

    private void tab_level_setup() {
        btnBackLevel.setVisibility(View.VISIBLE);
        btnBackLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        arrayList=new ArrayList<>();
        Cursor cursor = database.query("level",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            while (!cursor.isAfterLast()){
                arrayList.add(new Level(cursor.getInt(0),cursor.getString(1),
                        cursor.getInt(2),cursor.getInt(3),cursor.getInt(4)));
                cursor.moveToNext();
            }
        }

        LevelAdapter adapter=new LevelAdapter(LevelActivity.this,R.layout.layout_listview_level,arrayList);
        lvLevel.setAdapter(adapter);
        lvLevel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(LevelActivity.this,QuestionActivityLobby.class);
                Level level=arrayList.get(position);
                intent.putExtra("id","level");
                intent.putExtra("level_id",level.getLevel_id());
                intent.putExtra("min",level.getMinRequired());
                intent.putExtra("total",level.getTotalQuestion());
                intent.putExtra("name",level.getName());
                intent.putExtra("time",level.getTime());
                startActivity(intent);
            }
        });

    }
}