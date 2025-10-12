package com.example.afinal;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.afinal.dbclass.Categories;
import com.example.afinal.adapter.CategoriesAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private TabHost tabmain;
    private ListView lvTopic;
    private String DB_PATH_SUFFIX="/databases/";   // mặc định
    private String DATABASE_NAME= "ATGT.db"; //tên file
    private ArrayList<Categories> list;
    private SQLiteDatabase database=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        processCopy();
        database = openOrCreateDatabase("ATGT.db",MODE_PRIVATE,null);
        tabmainsetup();
        tab_topic_setup();

    }

    private void tab_topic_setup() {
        list=new ArrayList<>();
        Cursor cursor = database.query("categories",null,null,null,null,null,null);
        if(cursor.moveToFirst()) {
            while (!cursor.isAfterLast()){
                list.add(new Categories(cursor.getInt(0),
                        cursor.getString(1),cursor.getInt(2),cursor.getInt(3),cursor.getInt(4)));
                cursor.moveToNext();
            }

        }

        //View tabView = findViewById(R.id.tab_topic_main);
        lvTopic = findViewById(R.id.lv_topic);
        CategoriesAdapter adapter=new CategoriesAdapter(MainActivity.this,R.layout.layout_listview_topic,list);
        lvTopic.setAdapter(adapter);
        lvTopic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(MainActivity.this, QuestionActivityLobby.class);
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

    private void tabmainsetup() {
        tabmain=findViewById(R.id.tab_main);
        tabmain.setup();

        TabHost.TabSpec spec_topic,spec_level;
        spec_topic=tabmain.newTabSpec("topic");
        spec_level=tabmain.newTabSpec("level");
        spec_level.setContent(R.id.tab_level_main);
        spec_topic.setContent(R.id.tab_topic_main);
        spec_topic.setIndicator("Ôn tập theo chủ đề");
        spec_level.setIndicator("Ôn tập theo bằng cấp");
        tabmain.addTab(spec_topic);
        tabmain.addTab(spec_level);

    }
    private void processCopy() {
//private app
        File dbFile = getDatabasePath(DATABASE_NAME);
        if (!dbFile.exists())
        {
            try{CopyDataBaseFromAsset();
                Toast.makeText(this, "Copying sucess from Assets folder",
                        Toast.LENGTH_LONG).show();
            }
            catch (Exception e){
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }
    private String getDatabasePath() {
        return getApplicationInfo().dataDir + DB_PATH_SUFFIX+ DATABASE_NAME;
    }
    public void CopyDataBaseFromAsset() {
// TODO Auto-generated method stub
        try {
            InputStream myInput;
            myInput = getAssets().open(DATABASE_NAME);
// Path to the just created empty db
            String outFileName = getDatabasePath();
// if the path doesn't exist first, create it
            File f = new File(getApplicationInfo().dataDir + DB_PATH_SUFFIX);
            if (!f.exists())
                f.mkdir();
// Open the empty db as the output stream
            OutputStream myOutput = new FileOutputStream(outFileName);
// transfer bytes from the inputfile to the outputfile
// Truyền bytes dữ liệu từ input đến output
            int size = myInput.available();
            byte[] buffer = new byte[size];
            myInput.read(buffer);
            myOutput.write(buffer);
// Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();
        } catch (IOException e) {
//TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}