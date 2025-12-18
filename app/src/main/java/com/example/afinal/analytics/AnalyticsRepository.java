package com.example.afinal.analytics;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

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

        // Exam sessions table - Updated schema (only for mock exams)
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
                "status TEXT," +  // "Đỗ" or "Trượt"
                "level_id INTEGER," +
                "level_name TEXT," +
                "min_required INTEGER," +
                "total_questions INTEGER," +
                "device_info TEXT" +
                ")");
        
        // Migration: Add new columns if they don't exist (for existing databases)
        try {
            android.database.Cursor cursor = db.rawQuery("PRAGMA table_info(exam_sessions)", null);
            java.util.Set<String> columns = new java.util.HashSet<>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    columns.add(cursor.getString(1)); // column name is at index 1
                }
                cursor.close();
            }
            
            // Add missing columns
            if (!columns.contains("level_id")) {
                db.execSQL("ALTER TABLE exam_sessions ADD COLUMN level_id INTEGER");
            }
            if (!columns.contains("level_name")) {
                db.execSQL("ALTER TABLE exam_sessions ADD COLUMN level_name TEXT");
            }
            if (!columns.contains("min_required")) {
                db.execSQL("ALTER TABLE exam_sessions ADD COLUMN min_required INTEGER");
            }
            if (!columns.contains("total_questions")) {
                db.execSQL("ALTER TABLE exam_sessions ADD COLUMN total_questions INTEGER");
            }
            if (!columns.contains("device_info")) {
                db.execSQL("ALTER TABLE exam_sessions ADD COLUMN device_info TEXT");
            }
            if (!columns.contains("status")) {
                db.execSQL("ALTER TABLE exam_sessions ADD COLUMN status TEXT");
            }
        } catch (Exception e) {
            // Ignore migration errors - table might not exist yet
        }

        // Practice sessions table - For practice mode (topic-based)
        db.execSQL("CREATE TABLE IF NOT EXISTS practice_sessions (" +
                "session_id TEXT PRIMARY KEY," +
                "user_id TEXT NOT NULL," +
                "started_at_ms INTEGER," +
                "submitted_at_ms INTEGER," +
                "duration_ms INTEGER," +
                "score_raw INTEGER," +
                "score_pct REAL," +
                "num_correct INTEGER," +
                "num_incorrect INTEGER," +
                "num_critical_wrong INTEGER," +
                "topic_id INTEGER," +
                "topic_name TEXT," +
                "start_question INTEGER," +
                "end_question INTEGER," +
                "total_questions INTEGER," +
                "mode TEXT," +  // "practice_topic" or "ai_practice"
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
        SQLiteDatabase db = null;
        try {
            db = openDb();
            // Ensure schema is up to date (migration)
            ensureSchema();
            
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
            
            // Check if columns exist before inserting (safe for old databases)
            android.database.Cursor cursor = db.rawQuery("PRAGMA table_info(exam_sessions)", null);
            java.util.Set<String> columns = new java.util.HashSet<>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    columns.add(cursor.getString(1));
                }
                cursor.close();
            }
            
            // New fields for exam sessions - only insert if column exists
            if (data.get("status") != null && columns.contains("status")) {
                cv.put("status", (String) data.get("status"));
            }
            if (data.get("level_id") != null && columns.contains("level_id")) {
                cv.put("level_id", (Integer) data.get("level_id"));
            }
            if (data.get("level_name") != null && columns.contains("level_name")) {
                cv.put("level_name", (String) data.get("level_name"));
            }
            if (data.get("min_required") != null && columns.contains("min_required")) {
                cv.put("min_required", (Integer) data.get("min_required"));
            }
            if (data.get("total_questions") != null && columns.contains("total_questions")) {
                cv.put("total_questions", (Integer) data.get("total_questions"));
            }
            if (data.get("device_info") != null && columns.contains("device_info")) {
                cv.put("device_info", (String) data.get("device_info"));
            }
            
            db.insertWithOnConflict("exam_sessions", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLiteException e) {
            android.util.Log.e("AnalyticsRepository", "Error upserting exam session: " + e.getMessage(), e);
            // Try to migrate schema and retry once
            try {
                if (db != null) {
                    ensureSchema();
                    // Retry insert with basic fields only
                    ContentValues cvRetry = new ContentValues();
                    cvRetry.put("session_id", (String) data.get("session_id"));
                    cvRetry.put("user_id", (String) data.get("user_id"));
                    Object started = data.get("started_at_ms");
                    if (started == null) started = data.get("started_at");
                    if (started != null) cvRetry.put("started_at_ms", started instanceof Long ? (Long) started : ((Number) started).longValue());
                    Object submitted = data.get("submitted_at_ms");
                    if (submitted == null) submitted = data.get("submitted_at");
                    if (submitted != null) cvRetry.put("submitted_at_ms", submitted instanceof Long ? (Long) submitted : ((Number) submitted).longValue());
                    if (data.get("score_raw") != null) cvRetry.put("score_raw", (Integer) data.get("score_raw"));
                    if (data.get("num_correct") != null) cvRetry.put("num_correct", (Integer) data.get("num_correct"));
                    if (data.get("num_incorrect") != null) cvRetry.put("num_incorrect", (Integer) data.get("num_incorrect"));
                    db.insertWithOnConflict("exam_sessions", null, cvRetry, SQLiteDatabase.CONFLICT_REPLACE);
                }
            } catch (Exception e2) {
                android.util.Log.e("AnalyticsRepository", "Retry failed: " + e2.getMessage(), e2);
            }
        } finally {
            if (db != null) db.close();
        }
    }

    public void upsertPracticeSession(Map<String, Object> data) {
        SQLiteDatabase db = openDb();
        ContentValues cv = new ContentValues();
        cv.put("session_id", (String) data.get("session_id"));
        cv.put("user_id", (String) data.get("user_id"));
        Object started = data.get("started_at_ms");
        if (started == null) started = data.get("started_at");
        if (started != null) cv.put("started_at_ms", started instanceof Long ? (Long) started : ((Number) started).longValue());
        Object submitted = data.get("submitted_at_ms");
        if (submitted == null) submitted = data.get("submitted_at");
        if (submitted != null) cv.put("submitted_at_ms", submitted instanceof Long ? (Long) submitted : ((Number) submitted).longValue());
        if (data.get("duration_ms") != null) cv.put("duration_ms", (Long) data.get("duration_ms"));
        if (data.get("score_raw") != null) cv.put("score_raw", (Integer) data.get("score_raw"));
        if (data.get("score_pct") != null) cv.put("score_pct", (Double) data.get("score_pct"));
        if (data.get("num_correct") != null) cv.put("num_correct", (Integer) data.get("num_correct"));
        if (data.get("num_incorrect") != null) cv.put("num_incorrect", (Integer) data.get("num_incorrect"));
        Object numCriticalWrong = data.get("num_critical_wrong");
        if (numCriticalWrong == null) numCriticalWrong = data.get("num_liet_wrong");
        if (numCriticalWrong != null) cv.put("num_critical_wrong", (Integer) numCriticalWrong);
        if (data.get("topic_id") != null) cv.put("topic_id", (Integer) data.get("topic_id"));
        if (data.get("topic_name") != null) cv.put("topic_name", (String) data.get("topic_name"));
        if (data.get("start_question") != null) cv.put("start_question", (Integer) data.get("start_question"));
        if (data.get("end_question") != null) cv.put("end_question", (Integer) data.get("end_question"));
        if (data.get("total_questions") != null) cv.put("total_questions", (Integer) data.get("total_questions"));
        if (data.get("mode") != null) cv.put("mode", (String) data.get("mode"));
        if (data.get("device_info") != null) cv.put("device_info", (String) data.get("device_info"));
        db.insertWithOnConflict("practice_sessions", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
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
            try {
                // Newer schema: has created_at_ms column for ordering
                c = db.rawQuery(
                        "SELECT question_id FROM bookmarks WHERE user_id=? ORDER BY created_at_ms DESC",
                        new String[]{userId});
            } catch (SQLiteException e) {
                // Backward-compatible fallback: old DB without created_at_ms
                c = db.rawQuery(
                        "SELECT question_id FROM bookmarks WHERE user_id=?",
                        new String[]{userId});
            }
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


