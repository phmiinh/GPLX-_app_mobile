package com.example.afinal;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.afinal.analytics.AnalyticsRepository;
import com.example.afinal.analytics.FirestoreService;
import com.example.afinal.analytics.UserIdentity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class QuestionActivityBase extends AppCompatActivity {
    protected  TextView topicname;
    protected  ImageButton back;
    protected  Button submit;

    // Bookmark Component
    protected ImageButton bookmarkButton; // Khai báo nút Bookmark

    protected SQLiteDatabase database= null;
    protected  TextView content;
    protected  RadioButton a,b,c,d;
    protected ImageView imgQuestion;
    protected  String ans="",explaination="",img_url="",id,state="Trượt";
    protected  RadioGroup radioGroup;
    protected  ArrayList<Integer> listofquestion=new ArrayList<>();
    protected  HashMap<Integer,String> hashMap;
    protected  HashMap<Integer,String> answer;
    protected  int count;
    protected  int start,end,level,min,time,total,ques_id,critical=0,topicid;
    protected  Intent intent;
    protected  Cursor cursor=null;
    protected boolean isBookmarked = false; // Trạng thái đánh dấu của câu hỏi hiện tại

    protected  HashMap<Integer,Integer>rule;

    // Firebase Analytics & Firestore
    protected AnalyticsRepository analyticsRepository;
    protected FirestoreService firestoreService;
    protected String sessionId;
    protected long sessionStartAt;
    protected long questionStartAt;


    protected void init(){
        intent=getIntent();
        database=openOrCreateDatabase("ATGT.db",MODE_PRIVATE,null);
        get_from_intent();
        hashMap=new HashMap<>();
        answer=new HashMap<>();

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
        if(id != null && id.equals("topic")){
            start=intent.getIntExtra("start",1);
            end=intent.getIntExtra("end",1);
            count= end-start+1;
            topicid=intent.getIntExtra("categories_id",0);
            Log.d("Base", "topicid: "+topicid);

        }
        else if (id != null && id.equals("level")){
            level = intent.getIntExtra("level_id", 1);
            min = intent.getIntExtra("min", 1);
            count = intent.getIntExtra("total", 1);
            time = intent.getIntExtra("time", 1);
            rule = new HashMap<>();
        } else if (id != null && id.equals("bookmark")) {
            // Xử lý riêng trong QuestionActivityBookmark
        }
    }

    /**
     * Hàm này được gọi để thiết lập nút Bookmark và kiểm tra trạng thái đánh dấu.
     * Cần gọi hàm này trong onCreate() của các Activity con.
     */
    protected void setupBookmark(Context context) {
        if (bookmarkButton == null) return;

        // 1. Lắng nghe sự kiện click
        bookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBookmarkState(context);
            }
        });

        // 2. Tải trạng thái ban đầu của câu hỏi hiện tại
        loadBookmarkState(context);
    }

    /**
     * Tải trạng thái đánh dấu của câu hỏi hiện tại (ques_id) từ Firestore.
     */
    protected void loadBookmarkState(Context context) {
        String userId = UserIdentity.getUserId(this);
        if (ques_id > 0) {
            firestoreService.isQuestionBookmarked(userId, ques_id, new FirestoreService.BookmarkCheckListener() {
                @Override
                public void onResult(boolean isMarked) {
                    isBookmarked = isMarked;
                    updateBookmarkIcon(context);
                }

                @Override
                public void onError(Exception e) {
                    Log.e("Bookmark", "Error checking bookmark", e);
                    isBookmarked = false;
                    updateBookmarkIcon(context);
                }
            });
        }
    }


    /**
     * Thay đổi trạng thái đánh dấu của câu hỏi hiện tại và cập nhật lên Firestore.
     */
    protected void toggleBookmarkState(Context context) {
        String userId = UserIdentity.getUserId(this);
        if (ques_id <= 0) {
            Toast.makeText(context, "Không có câu hỏi để đánh dấu", Toast.LENGTH_SHORT).show();
            return;
        }

        isBookmarked = !isBookmarked;

        if (isBookmarked) {
            firestoreService.addBookmark(userId, ques_id,
                    aVoid -> Toast.makeText(context, "Đã thêm vào danh sách đánh dấu", Toast.LENGTH_SHORT).show(),
                    e -> Toast.makeText(context, "Lỗi khi thêm bookmark", Toast.LENGTH_SHORT).show()
            );
        } else {
            firestoreService.removeBookmark(userId, ques_id,
                    aVoid -> Toast.makeText(context, "Đã xóa khỏi danh sách đánh dấu", Toast.LENGTH_SHORT).show(),
                    e -> Toast.makeText(context, "Lỗi khi xóa bookmark", Toast.LENGTH_SHORT).show()
            );
        }

        updateBookmarkIcon(context);
    }


    /**
     * Cập nhật icon của nút Bookmark dựa trên trạng thái (đã đánh dấu hay chưa).
     */
    protected void updateBookmarkIcon(Context context) {
        if (bookmarkButton == null) return;
        if (isBookmarked) {
            // Icon đã đánh dấu (Solid - Màu xanh)
            bookmarkButton.setImageResource(R.drawable.ic_bookmark_marked);
        } else {
            // Icon chưa đánh dấu (Outline - Màu xám)
            bookmarkButton.setImageResource(R.drawable.ic_bookmark_unmarked);
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
            if(id != null && id.equals("topic")&&topicid==7){
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
        if (questionStartAt > 0 && id != null && (id.equals("topic") || id.equals("level"))) {
            logAttemptForCurrent();
        }

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

        // Cập nhật trạng thái Bookmark cho câu hỏi mới
        if (bookmarkButton != null) {
            loadBookmarkState(context);
        }
    }

    protected void logAttemptForCurrent() {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) return;
        RadioButton selected = findViewById(selectedId);
        if (selected == null) return;
        String chosen = selected.getText().toString();
        boolean correct = chosen.equals(ans);
        long now = System.currentTimeMillis();
        long spent = questionStartAt > 0 ? (now - questionStartAt) : 0L;
        boolean hasImg = img_url != null && !img_url.isEmpty();
        int topicId = 0;
        try {
            topicId = cursor.getInt(1);
        } catch (Exception ignored) {}

        Map<String, Object> record = new HashMap<>();
        record.put("user_id", UserIdentity.getUserId(this));
        record.put("question_id", ques_id);
        record.put("topic_id", topicId);
        record.put("is_correct", correct);
        record.put("time_spent_ms", spent);
        record.put("timestamp", now);
        record.put("session_id", sessionId);
        record.put("mode", id != null && id.equals("topic") ? "practice_topic" : "mock_exam");
        record.put("has_image", hasImg);

        // Save to local database
        analyticsRepository.insertAttempt(record);
        // Save to Firestore
        firestoreService.saveAttempt(record);

        // Save question meta
        Map<String, Object> qm = new HashMap<>();
        qm.put("topic_id", topicId);
        qm.put("is_critical", cursor.getInt(4) == 1);
        qm.put("has_image", hasImg);
        analyticsRepository.upsertQuestionMeta(ques_id, topicId, cursor.getInt(4) == 1, hasImg);
        firestoreService.upsertQuestionMeta(String.format(Locale.US, "%d", ques_id), qm);
    }
    protected void setCursor() {
        if(id != null && id.equals("topic")){
            if(topicid<7){
                cursor = database.query("Questions",null,"question_id BETWEEN ? AND ?",new String[]{String.valueOf(start),String.valueOf(end)},null,null,null);
            }
            else{
                cursor = database.query("Questions",null,"is_critical=?",new String[]{"1"},null,null,"question_id asc",String.valueOf(end));
            }
        }
        else if(id != null && id.equals("bookmark")){
            // Logic truy vấn sẽ được thực hiện trong QuestionActivityBookmark
            return;
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
        // Kiểm tra tránh lỗi khi database chưa được mở (ví dụ: trong unit test)
        if(database != null && database.isOpen()){
            database.close();
        }
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
                        // Log nỗ lực cuối cùng trước khi nộp bài
                        logAttemptForCurrent();
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
        if(id != null && id.equals("level")){
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
        session.put("started_at", sessionStartAt);
        session.put("submitted_at", now);
        session.put("duration_ms", now - sessionStartAt);
        session.put("blueprint_used", buildBlueprintUsed());
        session.put("score_raw", truecnt);
        session.put("score_pct", count == 0 ? 0.0 : (truecnt * 100.0) / count);
        session.put("num_correct", truecnt);
        session.put("num_incorrect", Math.max(0, count - truecnt));
        session.put("num_liet_wrong", numCriticalWrong);
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
        if(id != null && id.equals("topic")&&topicid==7){
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
}
