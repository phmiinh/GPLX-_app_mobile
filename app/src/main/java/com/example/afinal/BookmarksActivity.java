package com.example.afinal;

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
import com.example.afinal.analytics.AnalyticsRepository;
import com.example.afinal.analytics.UserIdentity;
import com.example.afinal.dbclass.Question;

import java.util.ArrayList;
import java.util.List;

public class BookmarksActivity extends AppCompatActivity {
    private ListView listView;
    private Button back;
    private SQLiteDatabase database = null;
    private AnalyticsRepository analyticsRepository;

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

        database = openOrCreateDatabase("ATGT.db", MODE_PRIVATE, null);
        analyticsRepository = new AnalyticsRepository(this);

        listView = findViewById(R.id.lvQAR);
        back = findViewById(R.id.btnQARback);

        ArrayList<Question> questions = new ArrayList<>();
        loadBookmarkedQuestions(questions);

        QuestionAdapter adapter = new QuestionAdapter(this, R.layout.layout_listview_review, questions);
        listView.setAdapter(adapter);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadBookmarkedQuestions(ArrayList<Question> out) {
        String userId = UserIdentity.getUserId(this);
        List<String> ids = analyticsRepository.getBookmarkedQuestionIds(userId);
        for (String id : ids) {
            Cursor cursor = database.query("questions", null, "question_id=?", new String[]{id}, null, null, null);
            if (!cursor.moveToFirst()) {
                cursor.close();
                continue;
            }
            Question q = new Question();
            q.setId(cursor.getInt(0));
            q.setContent(cursor.getString(2));
            q.setImg_url(cursor.getString(3));
            q.setExplain(cursor.getString(5));
            q.setA(cursor.getString(6));
            q.setB(cursor.getString(7));
            q.setC(cursor.getString(8));
            q.setD(cursor.getString(9));
            q.setAnswer(cursor.getString(10));
            q.setIs_critical(cursor.getInt(4));
            q.setUserChoice("");
            out.add(q);
            cursor.close();
        }
    }
}



