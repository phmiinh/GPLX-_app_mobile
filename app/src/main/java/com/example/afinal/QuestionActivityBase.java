package com.example.afinal;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import com.example.afinal.DAO.QuestionDAO;
import com.example.afinal.dbclass.Question;
import com.example.afinal.analytics.AnalyticsRepository;
import com.example.afinal.analytics.FirestoreService;
import com.example.afinal.analytics.UserIdentity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class QuestionActivityBase extends AppCompatActivity {
    protected TextView topicname, content, timer;
    protected ImageButton back;
    protected Button submit,next;
    protected SQLiteDatabase database = null;
    protected RadioButton a, b, c, d;
    protected ImageView imgQuestion;
    protected String img_url = "", id, state = "Trượt", msg, startTime, endTime, ans;
    protected ImageView bookmarkButton;
    protected RadioGroup radioGroup;
    protected int start, end, level, min, time, topicid, count, anInt = 0, ques_id, topicId1, is_critical;
    protected Intent intent;
    protected HashMap<Integer, Integer> rule;
    protected ArrayList<Question> listQuestion;
    protected QuestionDAO questionDAO;
    protected SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    protected DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    protected CountDownTimer countDownTimer;

    // Firebase Analytics
    protected AnalyticsRepository analyticsRepository;
    protected FirestoreService firestoreService;
    protected String sessionId;
    protected long sessionStartAt;
    protected long questionStartAt;
    protected int orderInSession = 0;  // Track order of question in session (starts at 0, becomes 1+ when logging)
    protected long sessionDurationMs = 0;  // Total session duration for mock_exam
    protected HashMap<Integer, Integer> questionAttemptCounts = new HashMap<>();  // Track attempts per question for auto-bookmark
    /**
     * Indicates whether the user requested an AI/hint explanation for
     * the current question. Consumed inside logAttemptForCurrent and
     * reset whenever a new question is displayed.
     */
    protected boolean aiUsedForCurrentQuestion = false;
    /**
     * Logical mode label for analytics: e.g. "practice_topic", "mock_exam",
     * "ai_practice", "ai_mock_exam". If null, we infer from id ("topic"/"level").
     */
    protected String sessionMode;
    protected void init(){
        intent=getIntent();
        startTime=getTime();
        database=openOrCreateDatabase("ATGT.db",MODE_PRIVATE,null);
        get_from_intent();
        questionDAO=new QuestionDAO(database);
        get_list_question();
    }
    public String getTime() {
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(formatter);
        return formattedDate;
    }
    private void get_list_question() {
        if(id.equals("topic")){
            if(topicid<7){
                listQuestion=questionDAO.getQuestionInRange(start,end);
            }
            else{
                listQuestion=questionDAO.getCriticalQuestionInRange(start,end);
            }
        }
        else {
            listQuestion=questionDAO.getQuestionOfLevel(level);
        }
        for(int i=0;i<listQuestion.size();i++){
            listQuestion.get(i).setContent("Câu "+String.valueOf(i+start)+": "+listQuestion.get(i).getContent());
        }

        
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
        // Optional higher-level session mode passed from callers (e.g. AI practice)
        sessionMode = intent.getStringExtra("ai_mode");
        if(id.equals("topic")){
            start=intent.getIntExtra("start",1);
            end=intent.getIntExtra("end",1);
            count= end-start+1;
            topicid=intent.getIntExtra("categories_id",0);
            Log.d("con cac", "topicid: "+topicid);
        }
        else {
            start=1;
            level = intent.getIntExtra("level_id", 1);
            min = intent.getIntExtra("min", 1);
            count = intent.getIntExtra("total", 1);
            Log.d("TAG", "onClick: " + count);
            time = intent.getIntExtra("time", 1);
            time*=60000;
            sessionDurationMs = time * 60 * 1000L;  // Convert minutes to ms
            rule = new HashMap<>();
        }
    }
    protected  void setting(Context context){
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull RadioGroup group, int checkedId) {
                if(next.getText().toString().equals("Câu tiếp theo")) return;
                if (checkedId != -1) {
                    RadioButton selected = findViewById(checkedId);
                    if (selected != null) {
                        String chosen = selected.getText().toString();
                        listQuestion.get(anInt).setUserChoice(chosen);
                    }
                }
            }
        });
        if(listQuestion.isEmpty()){
            Log.d("DEBUG_TAG", "Can't find data");
            finish();
        }

    }
    protected void set_content(Question question, Context context) {
        content.setText(question.getContent());
        a.setText(question.getA());
        b.setText(question.getB());
        ans=question.getAnswer();
        if (questionStartAt > 0) {
            logAttemptForCurrent();
        }
        is_critical=question.getIs_critical();
        // Increment order in session
        orderInSession++;
        topicId1=question.getTopic_id();
        ques_id=question.getId();
        
        c.setVisibility(View.VISIBLE);
        d.setVisibility(View.VISIBLE);
        if(question.getC()==null){
            c.setVisibility(View.GONE);
        }
        else c.setText(question.getC());
        if(question.getD()==null){
            d.setVisibility(View.GONE);
        }
        else d.setText(question.getD());
        img_url=question.getImg_url();
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
        // Reset AI usage flag for the new question
        aiUsedForCurrentQuestion = false;
        // Refresh bookmark icon for current question
        refreshBookmarkIcon();
    }

    protected void settingtimer(Context context) {
        if(id.equals("topic")){
            timer.setVisibility(View.GONE);
            return;
        }
        else{
            timer.setVisibility(View.VISIBLE);
            //time/=60;
            countDownTimer=new CountDownTimer((long)time,1000) {
                @Override
                public void onFinish() {
                    timer.setText("00:00");
                    showpoint(context);
                }

                @Override
                public void onTick(long millisUntilFinished) {
                    long min=millisUntilFinished/60000;
                    long sec=(millisUntilFinished/1000)%60;
                    timer.setText(String.format("%02d",min)+":"+String.format("%02d",sec));

                }
            };
            countDownTimer.start();

            return;
        }
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
            topicId = topicId1;  // Index 1 = category_id column in Questions table
        } catch (Exception ignored) {}

        Map<String, Object> record = new HashMap<>();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String userIdForLogs = (firebaseUser != null ? firebaseUser.getUid() : UserIdentity.getUserId(this));
        record.put("user_id", userIdForLogs);
        record.put("question_id", String.valueOf(ques_id));  // Changed to string
        record.put("topic_id", topicId);
        record.put("is_correct", correct);
        record.put("time_spent_ms", spent);
        record.put("timestamp_ms", now);  // Renamed from timestamp
        record.put("session_id", sessionId);
        String mode;
        if (sessionMode != null && !sessionMode.isEmpty()) {
            mode = sessionMode;
        } else {
            mode = id.equals("topic") ? "practice_topic" : "mock_exam";
        }
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
        record.put("hint_or_ai_used", aiUsedForCurrentQuestion);
        record.put("skipped", skipped);
        
        // Save to local database
        analyticsRepository.insertAttempt(record);
        // Save to Firestore
        firestoreService.saveAttempt(record);

        // Save question meta
        Map<String, Object> qm = new HashMap<>();
        qm.put("topic_id", topicId);
        boolean isCritical;
        if(is_critical==0) isCritical=false;
        else isCritical=true;
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
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null || firebaseUser.isAnonymous()) {
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng tính năng đánh dấu câu hỏi", Toast.LENGTH_SHORT).show();
            return;
        }
        // Use Firebase UID for consistency with Firestore rules and BookmarksActivity
        String userId = firebaseUser.getUid();
        long now = System.currentTimeMillis();
        
        Map<String, Object> bookmark = new HashMap<>();
        bookmark.put("user_id", userId);
        bookmark.put("question_id", questionId);
        bookmark.put("reason", reason);
        bookmark.put("created_at_ms", now);
        Log.d("Bookmark", "Saving bookmark: " + bookmark);
        
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
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
                        // Cancel timer to prevent race conditions
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        endTime=getTime();
                        // Use 'this' instead of context to ensure we're using the activity context
                        showpoint(QuestionActivityBase.this);
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
        // Wrap in try-catch to prevent crashes that would prevent dialog from showing
        try {
        if (questionStartAt > 0 ) {
            logAttemptForCurrent();
            }
        } catch (Exception e) {
            Log.e("QuestionActivityBase", "Error logging last question attempt", e);
            // Continue to show results dialog even if logging fails
        }
        
        AlertDialog.Builder builder1=new AlertDialog.Builder(context);
        builder1.setTitle("Kết quả");
        msg="/"+count;
        int truecnt=0;
        int numCriticalWrong = 0;
        
        for(Question question:listQuestion){
            if(question.getUserChoice()==null) continue;
            if(question.getAnswer().equals(question.getUserChoice())){
                truecnt++;
                if(question.getIs_critical()==1) state="Đỗ";
        
            }
            else{
                if(question.getIs_critical()==1) numCriticalWrong++;
            }
        }
        msg=String.valueOf(truecnt)+msg;
        String status = "Đỗ";  // Default status
        if(id.equals("level")){
            msg+="\n";
            msg+="Thời gian: "+getTimeTest();
            msg+="\n";
            msg+="Trạng thái: ";
            // Tính status: Đỗ nếu không sai câu điểm liệt VÀ đúng >= min_required
            if(numCriticalWrong > 0 || truecnt < min) {
                state = "Trượt";
                status = "Trượt";
            } else {
                state = "Đỗ";
                status = "Đỗ";
            }
            msg+=state;
        } else {
            // Practice mode: không có status đỗ/trượt
            status = null;
        }
        
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // For Firestore, we MUST use the authenticated user's UID, not a generated one
        // Firestore security rules require request.auth.uid == request.resource.data.user_id
        String userId;
        if (firebaseUser != null && !firebaseUser.isAnonymous()) {
            userId = firebaseUser.getUid(); // Use authenticated UID for Firestore
        } else {
            userId = UserIdentity.getUserId(this); // Fallback for local DB only
        }
        long now = System.currentTimeMillis();
        long durationMs = now - sessionStartAt;
        
        // Tách riêng: exam_session chỉ cho thi thử (level), practice_session cho luyện tập (topic)
        // Wrap database operations in try-catch to prevent crashes that would prevent dialog from showing
        String firestoreError = null;
        try {
            // Check authentication for Firestore
            if (firebaseUser == null || firebaseUser.isAnonymous()) {
                Log.w("QuestionActivityBase", "User not authenticated or anonymous - Firestore save may fail");
                firestoreError = "Chưa đăng nhập - không thể lưu lịch sử lên Firebase";
            }
            
            if (id.equals("level")) {
                // THI THỬ: Lưu vào exam_sessions
                Map<String, Object> examSession = new HashMap<>();
                examSession.put("session_id", sessionId);
                examSession.put("user_id", userId);
                examSession.put("started_at_ms", sessionStartAt);
                examSession.put("submitted_at_ms", now);
                examSession.put("duration_ms", durationMs);
                
                // blueprint_json
                if (rule != null && !rule.isEmpty()) {
                    examSession.put("blueprint_json", buildBlueprintJson());
        } else if (sessionMode != null && sessionMode.equals("ai_mock_exam") && listQuestion != null) {
            HashMap<String, Integer> blueprintAi = new HashMap<>();
            for (Question q : listQuestion) {
                String key = String.valueOf(q.getTopic_id());
                int current = blueprintAi.containsKey(key) ? blueprintAi.get(key) : 0;
                blueprintAi.put(key, current + 1);
            }
                    examSession.put("blueprint_json", blueprintAi);
        } else {
                    examSession.put("blueprint_json", new HashMap<String, Integer>());
        }
                
                examSession.put("score_raw", truecnt);
                examSession.put("score_pct", count == 0 ? 0.0 : (truecnt * 100.0) / count);
                examSession.put("num_correct", truecnt);
                examSession.put("num_incorrect", Math.max(0, count - truecnt));
                examSession.put("num_critical_wrong", numCriticalWrong);
                examSession.put("status", status);  // "Đỗ" or "Trượt"
                examSession.put("level_id", level);
            String levelName = intent.getStringExtra("name");
            if (levelName != null) {
                    examSession.put("level_name", levelName);
            }
                examSession.put("min_required", min);
                examSession.put("total_questions", count);
                
                // Save to local database first (this should always work)
                try {
                    analyticsRepository.upsertExamSession(examSession);
                    Log.d("QuestionActivityBase", "Exam session saved to local database successfully");
                } catch (Exception dbError) {
                    Log.e("QuestionActivityBase", "Error saving to local database", dbError);
                }
                
                // Save to Firestore (may fail if not authenticated or rules don't allow)
                // IMPORTANT: Ensure user_id matches authenticated user for Firestore rules
                if (firebaseUser != null && !firebaseUser.isAnonymous()) {
                    // Double-check that user_id matches authenticated user
                    if (!examSession.get("user_id").equals(firebaseUser.getUid())) {
                        Log.w("QuestionActivityBase", "user_id mismatch! Setting to authenticated UID");
                        examSession.put("user_id", firebaseUser.getUid());
                    }
                    firestoreService.saveExamSession(examSession).addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("QuestionActivityBase", "Firestore save failed: " + e.getMessage(), e);
                            String errorMsg;
                            if (e.getMessage() != null && e.getMessage().contains("PERMISSION_DENIED")) {
                                errorMsg = "Lỗi quyền truy cập Firebase. Vui lòng kiểm tra Firestore Security Rules hoặc đăng nhập lại.";
                            } else {
                                errorMsg = "Không lưu được lịch sử lên Firebase: " + e.getMessage();
                            }
                            // Show toast on main thread
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(QuestionActivityBase.this, errorMsg, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                } else {
                    firestoreError = "Cần đăng nhập để lưu lịch sử lên Firebase";
                }
            } else {
                // LUYỆN TẬP: Lưu vào practice_sessions
                Map<String, Object> practiceSession = new HashMap<>();
                practiceSession.put("session_id", sessionId);
                practiceSession.put("user_id", userId);
                practiceSession.put("started_at_ms", sessionStartAt);
                practiceSession.put("submitted_at_ms", now);
                practiceSession.put("duration_ms", durationMs);
                practiceSession.put("score_raw", truecnt);
                practiceSession.put("score_pct", count == 0 ? 0.0 : (truecnt * 100.0) / count);
                practiceSession.put("num_correct", truecnt);
                practiceSession.put("num_incorrect", Math.max(0, count - truecnt));
                practiceSession.put("num_critical_wrong", numCriticalWrong);
                
                // Practice mode metadata
                if (id.equals("topic")) {
                    practiceSession.put("topic_id", topicid);
                    // Có thể lấy tên topic từ database nếu cần
                    practiceSession.put("start_question", start);
                    practiceSession.put("end_question", end);
                    practiceSession.put("total_questions", count);
                    String mode = (sessionMode != null && !sessionMode.isEmpty()) ? sessionMode : "practice_topic";
                    practiceSession.put("mode", mode);
                }
                
                // Save to local database first
                try {
                    analyticsRepository.upsertPracticeSession(practiceSession);
                    Log.d("QuestionActivityBase", "Practice session saved to local database successfully");
                } catch (Exception dbError) {
                    Log.e("QuestionActivityBase", "Error saving to local database", dbError);
                }
                
                // Save to Firestore
                // IMPORTANT: Ensure user_id matches authenticated user for Firestore rules
                if (firebaseUser != null && !firebaseUser.isAnonymous()) {
                    // Double-check that user_id matches authenticated user
                    if (!practiceSession.get("user_id").equals(firebaseUser.getUid())) {
                        Log.w("QuestionActivityBase", "user_id mismatch! Setting to authenticated UID");
                        practiceSession.put("user_id", firebaseUser.getUid());
                    }
                    firestoreService.savePracticeSession(practiceSession).addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("QuestionActivityBase", "Firestore save failed: " + e.getMessage(), e);
                            String errorMsg;
                            if (e.getMessage() != null && e.getMessage().contains("PERMISSION_DENIED")) {
                                errorMsg = "Lỗi quyền truy cập Firebase. Vui lòng kiểm tra Firestore Security Rules hoặc đăng nhập lại.";
                            } else {
                                errorMsg = "Không lưu được lịch sử lên Firebase: " + e.getMessage();
                            }
                            // Show toast on main thread
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(QuestionActivityBase.this, errorMsg, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                } else {
                    firestoreError = "Cần đăng nhập để lưu lịch sử lên Firebase";
                }
            }
        } catch (Exception e) {
            // Log error but don't crash - still show results dialog to user
            Log.e("QuestionActivityBase", "Error saving session to database/Firestore", e);
            firestoreError = "Lỗi: " + e.getMessage();
        }
        
        // Store error message to show in dialog if needed
        final String finalFirestoreError = firestoreError;
        
        // Add Firestore error message to dialog if there was an error
        String dialogMessage = msg;
        if (finalFirestoreError != null) {
            dialogMessage += "\n\n⚠️ " + finalFirestoreError;
        }
        
        builder1.setMessage(dialogMessage);
        builder1.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Finish activity and return to previous screen
                finish();
            }
        });
        builder1.setPositiveButton("Xem lại bài làm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Navigate to review screen
                try {
                    Intent nextIntent=new Intent(QuestionActivityBase.this,QuestionActivityReview.class);
                nextIntent.putParcelableArrayListExtra("listQuestion",listQuestion);
                nextIntent.putExtra("result",msg);
                nextIntent.putExtra("startTime",startTime);
                nextIntent.putExtra("endTime",endTime);
                nextIntent.putExtra("time",getTimeTest());
                startActivity(nextIntent);
                    // Only finish after successfully starting the review activity
                finish();
                } catch (Exception e) {
                    Log.e("QuestionActivityBase", "Error navigating to review screen", e);
                    Toast.makeText(QuestionActivityBase.this, "Lỗi khi mở màn hình xem lại", Toast.LENGTH_SHORT).show();
                    // Don't finish if navigation fails - let user try again or exit manually
                }
            }
        });
        
        // Ensure dialog is shown even if there were errors
        try {
        AlertDialog alertDialog=builder1.create();
        alertDialog.show();
        } catch (Exception e) {
            Log.e("QuestionActivityBase", "Error showing results dialog", e);
            // Fallback: show toast and finish
            Toast.makeText(this, "Kết quả: " + msg, Toast.LENGTH_LONG).show();
        }
    }
    private String getTimeTest() {
        String ans="";
        try{
            Date d1=sdf.parse(startTime);
            Date d2=sdf.parse(endTime);
            long time=Math.abs(d1.getTime()-d2.getTime())/1000;
            ans+=String.format("%02d",(time/60))+" phút "+String.format("%02d",(time%60))+" giây";
            return  ans;
        }
        catch (Exception e){
            return null;
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
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null || firebaseUser.isAnonymous()) {
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng tính năng đánh dấu câu hỏi", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = UserIdentity.getUserId(this);
        boolean marked = analyticsRepository.isBookmarked(userId, String.valueOf(ques_id));
        if (marked) {
            analyticsRepository.deleteBookmark(userId, String.valueOf(ques_id));
            Toast.makeText(this, "Đã bỏ đánh dấu", Toast.LENGTH_SHORT).show();
        } else {
            // Use common saveBookmark method so it syncs to Firestore
            saveBookmark(String.valueOf(ques_id), "important");
            Toast.makeText(this, "Đã đánh dấu câu hỏi", Toast.LENGTH_SHORT).show();
        }
        refreshBookmarkIcon();
    }
}