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

import com.example.afinal.analytics.AnalyticsRepository;
import com.example.afinal.analytics.FirestoreService;
import com.example.afinal.analytics.UserIdentity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class QuestionActivityBase extends AppCompatActivity {
    protected  TextView topicname;
    protected  ImageButton back;
    protected  Button submit;

    protected SQLiteDatabase database= null;
    protected  TextView content;
    protected  RadioButton a,b,c,d;
    protected ImageView imgQuestion;
    protected ImageView bookmarkButton;
    protected  String ans="",explaination="",img_url="",id,state="Trượt";
    protected  RadioGroup radioGroup;
    protected  ArrayList<Integer> listofquestion=new ArrayList<>();
    protected  HashMap<Integer,String> hashMap;
    protected  HashMap<Integer,String> answer;
    protected  int count;
    protected  int start,end,level,min,time,total,ques_id,critical=0,topicid;
    protected  Intent intent;
    protected  Cursor cursor=null;

    protected  HashMap<Integer,Integer>rule;
    
    // Firebase Analytics
    protected AnalyticsRepository analyticsRepository;
    protected FirestoreService firestoreService;
    protected String sessionId;
    protected long sessionStartAt;
    protected long questionStartAt;
    protected int orderInSession = 0;  // Track order of question in session (starts at 0, becomes 1+ when logging)
    protected long sessionDurationMs = 0;  // Total session duration for mock_exam
    protected HashMap<Integer, Integer> questionAttemptCounts = new HashMap<>();  // Track attempts per question for auto-bookmark


    protected void init(){
        intent=getIntent();
        database=openOrCreateDatabase("ATGT.db",MODE_PRIVATE,null);
        get_from_intent();
        hashMap=new HashMap<>();
        answer=new HashMap<>();
        questionAttemptCounts=new HashMap<>();
        
        // Initialize Firebase Analytics
        analyticsRepository = new AnalyticsRepository(this);
        analyticsRepository.ensureSchema();
        firestoreService = new FirestoreService();
        sessionId = UUID.randomUUID().toString();
        sessionStartAt = System.currentTimeMillis();
        questionStartAt = 0;
    }
    protected void get_from_intent() {
        id=intent.getStringExtra("id");
        if(id.equals("topic")){
            start=intent.getIntExtra("start",1);
            end=intent.getIntExtra("end",1);
            count= end-start+1;
            topicid=intent.getIntExtra("categories_id",0);
            Log.d("con cac", "topicid: "+topicid);

        }
        else {
            level = intent.getIntExtra("level_id", 1);
            min = intent.getIntExtra("min", 1);
            count = intent.getIntExtra("total", 1);
            Log.d("TAG", "onClick: " + count);
            time = intent.getIntExtra("time", 1);
            sessionDurationMs = time * 60 * 1000L;  // Convert minutes to ms
            rule = new HashMap<>();
        }
    }
    protected void setting(Cursor cursor,Context context) {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull RadioGroup group, int checkedId) {
                if (checkedId != -1) {
                    RadioButton selected = findViewById(checkedId);
                    if (selected != null) {
                        String chosen = selected.getText().toString();
                        hashMap.put(ques_id, chosen);
                    }
                }
            }
        });
        if(cursor.moveToFirst()){
            int cnt=1;
            if(id.equals("topic")&&topicid==7){
                while(true){
                    if(cnt==start) break;
                    cnt++;
                    cursor.moveToNext();
                }
            }
        }
        else {
            Log.d("DEBUG_TAG", "Can't find data");
            finish();
        }


    }
    protected void set_content(Cursor cursor,Context context) {
        // Log attempt for previous question if there was one
        if (questionStartAt > 0) {
            logAttemptForCurrent();
        }
        
        // Increment order in session
        orderInSession++;
        
        ques_id=cursor.getInt(0);
        content.setText("Câu "+ques_id+": "+cursor.getString(2));
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
        if(cursor.getInt(4)==1) critical=ques_id;
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
                Toast.makeText(context, "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
            }
        }
        
        // Start timing for new question
        questionStartAt = System.currentTimeMillis();

        // Refresh bookmark icon for current question
        refreshBookmarkIcon();
    }
    
    protected void logAttemptForCurrent() {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        boolean skipped = (selectedId == -1);
        RadioButton selected = skipped ? null : findViewById(selectedId);
        String chosen = skipped ? "" : (selected != null ? selected.getText().toString() : "");
        boolean correct = !skipped && chosen.equals(ans);
        long now = System.currentTimeMillis();
        long spent = questionStartAt > 0 ? (now - questionStartAt) : 0L;
        boolean hasImg = img_url != null && !img_url.isEmpty();
        // Read category_id from database (index 1) and map to topic_id for Firebase
        // Database column: category_id → Firebase field: topic_id (same concept, different naming)
        int topicId = 0;
        try {
            topicId = cursor.getInt(1);  // Index 1 = category_id column in Questions table
        } catch (Exception ignored) {}

        Map<String, Object> record = new HashMap<>();
        record.put("user_id", UserIdentity.getUserId(this));
        record.put("question_id", String.valueOf(ques_id));  // Changed to string
        record.put("topic_id", topicId);
        record.put("is_correct", correct);
        record.put("time_spent_ms", spent);
        record.put("timestamp_ms", now);  // Renamed from timestamp
        record.put("session_id", sessionId);
        String mode = id.equals("topic") ? "practice_topic" : "mock_exam";
        record.put("mode", mode);
        record.put("has_image", hasImg);
        
        // New fields
        record.put("order_in_session", orderInSession);
        // remaining_time_ratio only for mock_exam
        if (mode.equals("mock_exam") && sessionDurationMs > 0) {
            long elapsed = now - sessionStartAt;
            long remaining = Math.max(0, sessionDurationMs - elapsed);
            double ratio = sessionDurationMs > 0 ? (double) remaining / sessionDurationMs : 0.0;
            record.put("remaining_time_ratio", Math.max(0.0, Math.min(1.0, ratio)));
        }
        record.put("time_of_day_bucket", getTimeOfDayBucket());
        record.put("hint_or_ai_used", false);  // TODO: Implement when AI hint feature is added
        record.put("skipped", skipped);
        
        // Save to local database
        analyticsRepository.insertAttempt(record);
        // Save to Firestore
        firestoreService.saveAttempt(record);

        // Save question meta
        Map<String, Object> qm = new HashMap<>();
        qm.put("topic_id", topicId);
        boolean isCritical = cursor.getInt(4) == 1;
        qm.put("is_critical", isCritical);
        qm.put("has_image", hasImg);
        analyticsRepository.upsertQuestionMeta(String.valueOf(ques_id), topicId, isCritical, hasImg);
        firestoreService.upsertQuestionMeta(String.valueOf(ques_id), qm);
        
        // Auto-bookmark logic: if is_critical and wrong >= 1, or any question wrong >= 2
        if (!correct) {
            int wrongCount = questionAttemptCounts.getOrDefault(ques_id, 0) + 1;
            questionAttemptCounts.put(ques_id, wrongCount);
            
            String bookmarkReason = null;
            if (isCritical && wrongCount >= 1) {
                bookmarkReason = "critical_risk";
            } else if (wrongCount >= 2) {
                bookmarkReason = "wrong_often";
            }
            
            if (bookmarkReason != null) {
                saveBookmark(String.valueOf(ques_id), bookmarkReason);
            }
        }
    }
    
    /**
     * Save bookmark to local and Firestore
     */
    protected void saveBookmark(String questionId, String reason) {
        String userId = UserIdentity.getUserId(this);
        long now = System.currentTimeMillis();
        
        Map<String, Object> bookmark = new HashMap<>();
        bookmark.put("user_id", userId);
        bookmark.put("question_id", questionId);
        bookmark.put("reason", reason);
        bookmark.put("created_at_ms", now);
        
        // Save to local database
        analyticsRepository.insertBookmark(userId, questionId, reason);
        // Save to Firestore
        firestoreService.saveBookmark(bookmark);
    }
    
    /**
     * Get time of day bucket: "sang" (5-11), "chieu" (12-17), "toi" (18-4)
     */
    private String getTimeOfDayBucket() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) {
            return "sang";
        } else if (hour >= 12 && hour < 18) {
            return "chieu";
        } else {
            return "toi";
        }
    }
    protected void setCursor() {
        if(id.equals("topic")){
            if(topicid<7){
                cursor = database.query("Questions",null,"question_id BETWEEN ? AND ?",new String[]{String.valueOf(start),String.valueOf(end)},null,null,null);
            }
            else{
                cursor = database.query("Questions",null,"is_critical=?",new String[]{"1"},null,null,"question_id asc",String.valueOf(end));
            }
        }
        else{
            get_rule();
            String sql="SELECT * from(\n" +
                    "\tselect * from(\n" +
                    "\t\tSELECT * from Questions\n" +
                    "\t\twhere is_critical=1\n" +
                    "\t\torder by random()\n" +
                    "\t\tlimit 1\n" +
                    "\t)\n" +
                    "\tunion all\n" +
                    "\tselect * from(\n" +
                    "\t\tSELECT * from Questions\n" +
                    "\t\twhere category_id=1 and is_critical=0\n" +
                    "\t\torder by random()\n" +
                    "\t\tlimit " + rule.getOrDefault(1,1)+
                    "\t)\n" +
                    "\tunion all\n" +
                    "\tselect * from(\n" +
                    "\t\tSELECT * from Questions\n" +
                    "\t\twhere category_id=2 and is_critical=0\n" +
                    "\t\torder by random()\n" +
                    "\t\tlimit " + rule.getOrDefault(2,1)+
                    "\t)\n" +
                    "\tunion all\n" +
                    "\tselect * from(\n" +
                    "\t\tSELECT * from Questions\n" +
                    "\t\twhere category_id=3 and is_critical=0\n" +
                    "\t\torder by random()\n" +
                    "\t\tlimit " + rule.getOrDefault(3,1)+
                    "\t)\n" +
                    "\tunion all\n" +
                    "\tselect * from(\n" +
                    "\t\tSELECT * from Questions\n" +
                    "\t\twhere category_id=4 and is_critical=0\n" +
                    "\t\torder by random()\n" +
                    "\t\tlimit " + rule.getOrDefault(4,1)+
                    "\t)\n" +
                    "\tunion all\n" +
                    "\tselect * from(\n" +
                    "\t\tSELECT * from Questions\n" +
                    "\t\twhere category_id=5 and is_critical=0\n" +
                    "\t\torder by random()\n" +
                    "\t\tlimit " + rule.getOrDefault(5,1)+
                    "\t)\n" +
                    "\tunion all\n" +
                    "\tselect * from(\n" +
                    "\t\tSELECT * from Questions\n" +
                    "\t\twhere category_id=6 and is_critical=0\n" +
                    "\t\torder by random()\n" +
                    "\t\tlimit " + rule.getOrDefault(6,1)+
                    "\t)\n" +
                    "\n" +
                    ")\n" +
                    "order by random()";
            cursor=database.rawQuery(sql,null);
        }
    }

    protected void get_rule() {
        Cursor cursor1=database.query("rules_exam",null,"level_id=?",new String[]{String.valueOf(level)},null,null,null);
        if(cursor1.moveToFirst()){
            while (!cursor1.isAfterLast()){
                rule.put(cursor1.getInt(1),cursor1.getInt(2));
                cursor1.moveToNext();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        database.close();
    }
    protected void submitSetup(Context context) {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder= new AlertDialog.Builder(context);
                builder.setTitle("Bạn chắc chắn muốn nộp bài chứ?");
                builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showpoint(context);
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
    protected void showpoint(Context context) {
        // Log last question attempt if not already logged
        if (questionStartAt > 0 && cursor != null) {
            logAttemptForCurrent();
        }
        
        AlertDialog.Builder builder1=new AlertDialog.Builder(context);
        builder1.setTitle("Kết quả");
        String msg="/"+count;
        int truecnt=0;
        int numCriticalWrong = 0;
        for(Integer q:hashMap.keySet()){
            if(hashMap.get(q).equals(answer.get(q))) {
                truecnt++;
                if(q==critical){
                    state="Đỗ";
                }
            } else {
                if(q==critical){
                    numCriticalWrong++;
                }
            }
        }
        msg=String.valueOf(truecnt)+msg;
        if(id.equals("level")){
            msg+="\n";
            msg+="Trạng thái: ";
            if(truecnt<min) state="Trượt";
            msg+=state;
        }
        
        // Save exam session to Firebase
        Map<String, Object> session = new HashMap<>();
        String userId = UserIdentity.getUserId(this);
        long now = System.currentTimeMillis();
        session.put("session_id", sessionId);
        session.put("user_id", userId);
        session.put("started_at_ms", sessionStartAt);  // Renamed from started_at
        session.put("submitted_at_ms", now);  // Renamed from submitted_at
        session.put("duration_ms", now - sessionStartAt);
        // blueprint_json as Map (preferred) or legacy string format
        if (id.equals("level") && rule != null && !rule.isEmpty()) {
            session.put("blueprint_json", buildBlueprintJson());  // Map format
        } else {
            // For practice_topic, blueprint_json is null or empty
            session.put("blueprint_json", new HashMap<String, Integer>());
        }
        session.put("score_raw", truecnt);
        session.put("score_pct", count == 0 ? 0.0 : (truecnt * 100.0) / count);
        session.put("num_correct", truecnt);
        session.put("num_incorrect", Math.max(0, count - truecnt));
        session.put("num_critical_wrong", numCriticalWrong);
        analyticsRepository.upsertExamSession(session);
        firestoreService.saveExamSession(session);
        
        builder1.setMessage(msg);
        builder1.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder1.setPositiveButton("Xem lại bài làm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent nextIntent=new Intent(context,QuestionActivityReview.class);
                getfullques();
                nextIntent.putExtra("choice",hashMap);
                nextIntent.putExtra("list",listofquestion);
                startActivity(nextIntent);
                finish();
            }
        });
        AlertDialog alertDialog=builder1.create();
        alertDialog.show();
    }
    protected   void getfullques(){
        cursor.moveToFirst();
        if(id.equals("topic")&&topicid==7){
            int cnt=1;
            while(true){
                if(cnt==start) break;
                cnt++;
                cursor.moveToNext();
            }
        }
        while(!cursor.isAfterLast()){
            listofquestion.add(cursor.getInt(0));
            cursor.moveToNext();
        }
    }
    
    /**
     * Build blueprint as Map (for blueprint_json field)
     * Returns Map with topic_id as string key and count as integer value
     */
    protected Map<String, Integer> buildBlueprintJson() {
        Map<String, Integer> blueprint = new HashMap<>();
        if (rule != null && !rule.isEmpty()) {
            for (Integer k : rule.keySet()) {
                blueprint.put(String.valueOf(k), rule.get(k));
            }
        }
        return blueprint;
    }
    
    /**
     * Legacy method for backward compatibility - converts to string format
     */
    protected String buildBlueprintUsed() {
        if (rule == null || rule.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Integer k : rule.keySet()) {
            if (!first) sb.append(",");
            sb.append(k).append(":").append(rule.get(k));
            first = false;
        }
        return sb.toString();
    }

    protected void backSetup(Context context) {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder= new AlertDialog.Builder(context);
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

    protected void setupBookmarkButton() {
        if (bookmarkButton == null) return;
        bookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBookmark();
            }
        });
        refreshBookmarkIcon();
    }

    protected void refreshBookmarkIcon() {
        if (bookmarkButton == null || analyticsRepository == null) return;
        String userId = UserIdentity.getUserId(this);
        boolean marked = analyticsRepository.isBookmarked(userId, String.valueOf(ques_id));
        bookmarkButton.setImageResource(marked ? R.drawable.ic_bookmark : R.drawable.ic_bookmark_border);
    }

    protected void toggleBookmark() {
        if (analyticsRepository == null) return;
        String userId = UserIdentity.getUserId(this);
        boolean marked = analyticsRepository.isBookmarked(userId, String.valueOf(ques_id));
        if (marked) {
            analyticsRepository.deleteBookmark(userId, String.valueOf(ques_id));
            Toast.makeText(this, "Đã bỏ đánh dấu", Toast.LENGTH_SHORT).show();
        } else {
            analyticsRepository.insertBookmark(userId, String.valueOf(ques_id), "important");
            Toast.makeText(this, "Đã đánh dấu câu hỏi", Toast.LENGTH_SHORT).show();
        }
        refreshBookmarkIcon();
    }
}