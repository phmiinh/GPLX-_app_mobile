package com.example.afinal;

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

public class QuestionActivityLast extends AppCompatActivity {
    private TextView topicname;
    private ImageButton back;
    private Button submit,next,prev;

    private SQLiteDatabase database= null;
    private TextView content;
    private RadioButton a,b,c,d;
    private ImageView imgQuestion;
    private String ans="",explaination="",img_url="",id,state="Trượt";
    private RadioGroup radioGroup;
    private ArrayList<Integer> listofquestion=new ArrayList<>();
    private HashMap<Integer,String> hashMap;
    private HashMap<Integer,String> answer;
    private int count;
    private int start,end,level,min,time,total,ques_id,critical=0,topicid;
    private  Intent intent;
    private Cursor cursor=null;
    private HashMap<Integer,Integer>rule;
    private String sessionId;
    private long sessionStartAt;
    private long questionStartAt;
    private AnalyticsRepository analyticsRepository;
    private FirestoreService firestoreService;
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
        intent=getIntent();

        String topic=intent.getStringExtra("name");
        database=openOrCreateDatabase("ATGT.db",MODE_PRIVATE,null);
        analyticsRepository = new AnalyticsRepository(this);
        analyticsRepository.ensureSchema();
        firestoreService = new FirestoreService();
        sessionId = UUID.randomUUID().toString();
        sessionStartAt = System.currentTimeMillis();
        topicname=findViewById(R.id.txtTopicQAL);
        id=intent.getStringExtra("id");
        if(id.equals("level")) topicname.setText("Hạng "+topic);
        else topicname.setText(topic);
        backSetup();


        setCursor();
        setting(cursor);
        submitSetup();
    }

    private void setCursor() {
        if(id.equals("topic")){
            start=intent.getIntExtra("start",1);
            end=intent.getIntExtra("end",1);
            count= end-start+1;
            topicid=intent.getIntExtra("categories_id",0);
            Log.d("con cac", "topicid: "+topicid);
            if(topicid<7){
                cursor = database.query("Questions",null,"question_id BETWEEN ? AND ?",new String[]{String.valueOf(start),String.valueOf(end)},null,null,null);
            }
            else{
                cursor = database.query("Questions",null,"is_critical=?",new String[]{"1"},null,null,"question_id asc",String.valueOf(end));
            }
        }
        else{
            level=intent.getIntExtra("level_id",1);
            min=intent.getIntExtra("min",1);
            count=intent.getIntExtra("total",1);

            Log.d("TAG", "onClick: "+count);
            time=intent.getIntExtra("time",1);
            rule=new HashMap<>();
            Cursor cursor1=database.query("rules_exam",null,"level_id=?",new String[]{String.valueOf(level)},null,null,null);
            if(cursor1.moveToFirst()){
                while (!cursor1.isAfterLast()){
                    rule.put(cursor1.getInt(1),cursor1.getInt(2));
                    cursor1.moveToNext();
                }
            }
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


    @Override
    protected void onDestroy() {
        super.onDestroy();

        database.close();
    }

    private void setting(Cursor cursor) {

        content=findViewById(R.id.txtQALcontent);
        a=findViewById(R.id.radiobtnQALa);
        b=findViewById(R.id.radiobtnQALb);
        c=findViewById(R.id.radiobtnQALc);
        d=findViewById(R.id.radiobtnQALd);
        next=findViewById(R.id.btnnextQAL);
        prev=findViewById(R.id.btnprevQAL);
        radioGroup=findViewById(R.id.radioBtnQAL);
        imgQuestion=findViewById(R.id.imgQAL);
        hashMap=new HashMap<>();
        answer=new HashMap<>();

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
            set_content(cursor);
            answer.put(ques_id,ans);
        }
        else {
            Log.d("DEBUG_TAG", "Can't find data");
            finish();
        }
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cursor.isFirst()) return;
                else{
                    cursor.moveToPrevious();
                    set_content(cursor);
                    answer.put(ques_id,ans);
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cursor.isLast()) return;
                else{
                    logAttemptForCurrent();
                    cursor.moveToNext();
                    set_content(cursor);
                    answer.put(ques_id,ans);
                }
            }
        });

    }

    private void logAttemptForCurrent() {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) return;
        RadioButton selected = findViewById(selectedId);
        if (selected == null) return;
        String chosen = selected.getText().toString();
        boolean correct = chosen.equals(ans);
        long now = System.currentTimeMillis();
        long spent = questionStartAt > 0 ? (now - questionStartAt) : 0L;
        boolean hasImg = img_url != null;
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
        record.put("mode", id.equals("topic") ? "practice_topic" : "mock_exam");
        record.put("has_image", hasImg);
        analyticsRepository.insertAttempt(record);
        firestoreService.saveAttempt(record);

        Map<String, Object> qm = new HashMap<>();
        qm.put("topic_id", topicId);
        qm.put("is_critical", cursor.getInt(4) == 1);
        qm.put("has_image", hasImg);
        analyticsRepository.upsertQuestionMeta(ques_id, topicId, cursor.getInt(4) == 1, hasImg);
        firestoreService.upsertQuestionMeta(String.format(Locale.US, "%d", ques_id), qm);
    }

    private String buildBlueprintUsed() {
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

    private void set_content(Cursor cursor) {
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
        if(cursor.getInt(4)==1) critical=ques_id;
        if(ansc==null){
            c.setVisibility(View.GONE);
        }
        else c.setText(ansc);
        if(ansd==null){
            d.setVisibility(View.GONE);
        }
        else d.setText(ansd);

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
                Toast.makeText(QuestionActivityLast.this, "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
            }
        }

        // Reset answer visuals to default for new question
        AnswerColorHelper.resetAnswerColors(a,b,c,d);

        questionStartAt = System.currentTimeMillis();

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
    }

    private  void getfullques(){
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
    private void submitSetup() {
        submit=findViewById(R.id.btnQAL_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder= new AlertDialog.Builder(QuestionActivityLast.this);
                builder.setTitle("Bạn chắc chắn muốn nộp bài chứ?");
                builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showpoint();
                    }

                    private void showpoint() {
                        AlertDialog.Builder builder1=new AlertDialog.Builder(QuestionActivityLast.this);
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
                        builder1.setMessage(msg);
                        // Save session summary locally + Firestore
                        Map<String, Object> session = new HashMap<>();
                        String userId = UserIdentity.getUserId(QuestionActivityLast.this);
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
                        builder1.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                        builder1.setPositiveButton("Xem lại bài làm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent nextIntent=new Intent(QuestionActivityLast.this,QuestionActivityReview.class);
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

    private void backSetup() {
        back=findViewById(R.id.btnBackQAL);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder= new AlertDialog.Builder(QuestionActivityLast.this);
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