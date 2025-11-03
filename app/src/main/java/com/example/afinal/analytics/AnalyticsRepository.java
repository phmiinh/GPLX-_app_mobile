package com.example.afinal.analytics;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONObject;

import java.util.Map;

public class AnalyticsRepository {
    private final Context context;

    public AnalyticsRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    private SQLiteDatabase openDb() {
        return context.openOrCreateDatabase("ATGT.db", Context.MODE_PRIVATE, null);
    }

    public void ensureSchema() {
        SQLiteDatabase db = openDb();
        // Attempts table - Updated schema with new fields
        db.execSQL("CREATE TABLE IF NOT EXISTS attempts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id TEXT NOT NULL," +
                "question_id TEXT NOT NULL," +  // Changed to TEXT
                "topic_id INTEGER," +
                "is_correct INTEGER NOT NULL," +
                "time_spent_ms INTEGER," +
                "timestamp_ms INTEGER NOT NULL," +  // Renamed from timestamp
                "session_id TEXT," +
                "mode TEXT," +
                "has_image INTEGER," +
                "order_in_session INTEGER," +  // New field
                "remaining_time_ratio REAL," +  // New field (0..1)
                "time_of_day_bucket TEXT," +  // New field: "sang"|"chieu"|"toi"
                "hint_or_ai_used INTEGER," +  // New field (boolean as int)
                "skipped INTEGER" +  // New field (boolean as int)
                ")");

        // Exam sessions table - Updated schema
        db.execSQL("CREATE TABLE IF NOT EXISTS exam_sessions (" +
                "session_id TEXT PRIMARY KEY," +
                "user_id TEXT NOT NULL," +
                "started_at_ms INTEGER," +  // Renamed from started_at
                "submitted_at_ms INTEGER," +  // Renamed from submitted_at
                "duration_ms INTEGER," +
                "blueprint_json TEXT," +  // Changed from blueprint_used (string) to JSON string
                "score_raw INTEGER," +
                "score_pct REAL," +
                "num_correct INTEGER," +
                "num_incorrect INTEGER," +
                "num_critical_wrong INTEGER," +
                "device_info TEXT" +
                ")");

        // Bookmarks table - Updated schema
        db.execSQL("CREATE TABLE IF NOT EXISTS bookmarks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id TEXT NOT NULL," +
                "question_id TEXT NOT NULL," +  // Changed to TEXT
                "reason TEXT," +  // "critical_risk"|"wrong_often"|"important"|"note"
                "created_at_ms INTEGER NOT NULL" +  // Renamed from created_at
                ")");

        // Question meta table - Updated schema
        db.execSQL("CREATE TABLE IF NOT EXISTS question_meta (" +
                "question_id TEXT PRIMARY KEY," +  // Changed to TEXT
                "topic_id INTEGER," +
                "is_critical INTEGER," +
                "has_image INTEGER," +
                "difficulty_tag TEXT" +  // Optional: "easy"|"medium"|"hard"
                ")");
        db.close();
    }

    public void insertAttempt(Map<String, Object> data) {
        SQLiteDatabase db = openDb();
        ContentValues cv = new ContentValues();
        cv.put("user_id", (String) data.get("user_id"));
        // question_id is now string
        Object qid = data.get("question_id");
        cv.put("question_id", qid != null ? String.valueOf(qid) : null);
        if (data.get("topic_id") != null) cv.put("topic_id", (Integer) data.get("topic_id"));
        cv.put("is_correct", ((Boolean) data.get("is_correct")) ? 1 : 0);
        if (data.get("time_spent_ms") != null) cv.put("time_spent_ms", (Long) data.get("time_spent_ms"));
        // timestamp_ms instead of timestamp
        Object ts = data.get("timestamp_ms");
        if (ts == null) ts = data.get("timestamp"); // backward compatibility
        if (ts != null) cv.put("timestamp_ms", ts instanceof Long ? (Long) ts : ((Number) ts).longValue());
        if (data.get("session_id") != null) cv.put("session_id", (String) data.get("session_id"));
        if (data.get("mode") != null) cv.put("mode", (String) data.get("mode"));
        if (data.get("has_image") != null) cv.put("has_image", ((Boolean) data.get("has_image")) ? 1 : 0);
        // New fields
        if (data.get("order_in_session") != null) cv.put("order_in_session", (Integer) data.get("order_in_session"));
        if (data.get("remaining_time_ratio") != null) cv.put("remaining_time_ratio", ((Number) data.get("remaining_time_ratio")).doubleValue());
        if (data.get("time_of_day_bucket") != null) cv.put("time_of_day_bucket", (String) data.get("time_of_day_bucket"));
        if (data.get("hint_or_ai_used") != null) cv.put("hint_or_ai_used", ((Boolean) data.get("hint_or_ai_used")) ? 1 : 0);
        if (data.get("skipped") != null) cv.put("skipped", ((Boolean) data.get("skipped")) ? 1 : 0);
        db.insert("attempts", null, cv);
        db.close();
    }

    public void upsertQuestionMeta(String questionId, int topicId, boolean isCritical, boolean hasImage) {
        SQLiteDatabase db = openDb();
        ContentValues cv = new ContentValues();
        cv.put("question_id", questionId);  // Now string
        cv.put("topic_id", topicId);
        cv.put("is_critical", isCritical ? 1 : 0);
        cv.put("has_image", hasImage ? 1 : 0);
        db.insertWithOnConflict("question_meta", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void upsertExamSession(Map<String, Object> data) {
        SQLiteDatabase db = openDb();
        ContentValues cv = new ContentValues();
        cv.put("session_id", (String) data.get("session_id"));
        cv.put("user_id", (String) data.get("user_id"));
        // Renamed fields with backward compatibility
        Object started = data.get("started_at_ms");
        if (started == null) started = data.get("started_at");
        if (started != null) cv.put("started_at_ms", started instanceof Long ? (Long) started : ((Number) started).longValue());
        Object submitted = data.get("submitted_at_ms");
        if (submitted == null) submitted = data.get("submitted_at");
        if (submitted != null) cv.put("submitted_at_ms", submitted instanceof Long ? (Long) submitted : ((Number) submitted).longValue());
        if (data.get("duration_ms") != null) cv.put("duration_ms", (Long) data.get("duration_ms"));
        // blueprint_json - convert Map to JSON string
        Object blueprint = data.get("blueprint_json");
        if (blueprint == null) blueprint = data.get("blueprint_used"); // backward compatibility
        if (blueprint != null) {
            if (blueprint instanceof Map) {
                try {
                    cv.put("blueprint_json", new JSONObject((Map) blueprint).toString());
                } catch (Exception e) {
                    cv.put("blueprint_json", blueprint.toString());
                }
            } else {
                cv.put("blueprint_json", blueprint.toString());
            }
        }
        if (data.get("score_raw") != null) cv.put("score_raw", (Integer) data.get("score_raw"));
        if (data.get("score_pct") != null) cv.put("score_pct", (Double) data.get("score_pct"));
        if (data.get("num_correct") != null) cv.put("num_correct", (Integer) data.get("num_correct"));
        if (data.get("num_incorrect") != null) cv.put("num_incorrect", (Integer) data.get("num_incorrect"));
        Object numCriticalWrong = data.get("num_critical_wrong");
        if (numCriticalWrong == null) numCriticalWrong = data.get("num_liet_wrong"); // backward compatibility
        if (numCriticalWrong != null) cv.put("num_critical_wrong", (Integer) numCriticalWrong);
        if (data.get("device_info") != null) cv.put("device_info", (String) data.get("device_info"));
        db.insertWithOnConflict("exam_sessions", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }
    
    public void insertBookmark(String userId, String questionId, String reason) {
        SQLiteDatabase db = openDb();
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("question_id", questionId);
        cv.put("reason", reason);  // "critical_risk"|"wrong_often"|"important"|"note"
        cv.put("created_at_ms", System.currentTimeMillis());
        db.insert("bookmarks", null, cv);
        db.close();
    }

    public void deleteBookmark(String userId, String questionId) {
        SQLiteDatabase db = openDb();
        db.delete("bookmarks", "user_id=? AND question_id=?", new String[]{userId, questionId});
        db.close();
    }

    public boolean isBookmarked(String userId, String questionId) {
        SQLiteDatabase db = openDb();
        boolean exists = false;
        android.database.Cursor c = null;
        try {
            c = db.rawQuery("SELECT 1 FROM bookmarks WHERE user_id=? AND question_id=? LIMIT 1",
                    new String[]{userId, questionId});
            exists = c.moveToFirst();
        } finally {
            if (c != null) c.close();
            db.close();
        }
        return exists;
    }

    public java.util.List<String> getBookmarkedQuestionIds(String userId) {
        SQLiteDatabase db = openDb();
        java.util.ArrayList<String> ids = new java.util.ArrayList<>();
        android.database.Cursor c = null;
        try {
            c = db.rawQuery("SELECT question_id FROM bookmarks WHERE user_id=? ORDER BY created_at_ms DESC",
                    new String[]{userId});
            while (c.moveToNext()) {
                ids.add(c.getString(0));
            }
        } finally {
            if (c != null) c.close();
            db.close();
        }
        return ids;
    }
}


