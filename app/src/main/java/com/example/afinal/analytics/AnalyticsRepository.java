package com.example.afinal.analytics;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

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
        db.execSQL("CREATE TABLE IF NOT EXISTS attempts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id TEXT NOT NULL," +
                "question_id INTEGER NOT NULL," +
                "topic_id INTEGER," +
                "is_correct INTEGER NOT NULL," +
                "time_spent_ms INTEGER," +
                "timestamp INTEGER NOT NULL," +
                "session_id TEXT," +
                "mode TEXT," +
                "has_image INTEGER" +
                ")");

        db.execSQL("CREATE TABLE IF NOT EXISTS exam_sessions (" +
                "session_id TEXT PRIMARY KEY," +
                "user_id TEXT NOT NULL," +
                "started_at INTEGER," +
                "submitted_at INTEGER," +
                "duration_ms INTEGER," +
                "blueprint_used TEXT," +
                "score_raw INTEGER," +
                "score_pct REAL," +
                "num_correct INTEGER," +
                "num_incorrect INTEGER," +
                "num_liet_wrong INTEGER," +
                "device_info TEXT" +
                ")");

        db.execSQL("CREATE TABLE IF NOT EXISTS bookmarks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id TEXT NOT NULL," +
                "question_id INTEGER NOT NULL," +
                "reason TEXT," +
                "created_at INTEGER NOT NULL" +
                ")");

        db.execSQL("CREATE TABLE IF NOT EXISTS question_meta (" +
                "question_id INTEGER PRIMARY KEY," +
                "topic_id INTEGER," +
                "is_critical INTEGER," +
                "has_image INTEGER" +
                ")");
        db.close();
    }

    public void insertAttempt(Map<String, Object> data) {
        SQLiteDatabase db = openDb();
        ContentValues cv = new ContentValues();
        cv.put("user_id", (String) data.get("user_id"));
        cv.put("question_id", (Integer) data.get("question_id"));
        if (data.get("topic_id") != null) cv.put("topic_id", (Integer) data.get("topic_id"));
        cv.put("is_correct", ((Boolean) data.get("is_correct")) ? 1 : 0);
        if (data.get("time_spent_ms") != null) cv.put("time_spent_ms", (Long) data.get("time_spent_ms"));
        cv.put("timestamp", (Long) data.get("timestamp"));
        if (data.get("session_id") != null) cv.put("session_id", (String) data.get("session_id"));
        if (data.get("mode") != null) cv.put("mode", (String) data.get("mode"));
        if (data.get("has_image") != null) cv.put("has_image", ((Boolean) data.get("has_image")) ? 1 : 0);
        db.insert("attempts", null, cv);
        db.close();
    }

    public void upsertQuestionMeta(int questionId, int topicId, boolean isCritical, boolean hasImage) {
        SQLiteDatabase db = openDb();
        ContentValues cv = new ContentValues();
        cv.put("question_id", questionId);
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
        if (data.get("started_at") != null) cv.put("started_at", (Long) data.get("started_at"));
        if (data.get("submitted_at") != null) cv.put("submitted_at", (Long) data.get("submitted_at"));
        if (data.get("duration_ms") != null) cv.put("duration_ms", (Long) data.get("duration_ms"));
        if (data.get("blueprint_used") != null) cv.put("blueprint_used", (String) data.get("blueprint_used"));
        if (data.get("score_raw") != null) cv.put("score_raw", (Integer) data.get("score_raw"));
        if (data.get("score_pct") != null) cv.put("score_pct", (Double) data.get("score_pct"));
        if (data.get("num_correct") != null) cv.put("num_correct", (Integer) data.get("num_correct"));
        if (data.get("num_incorrect") != null) cv.put("num_incorrect", (Integer) data.get("num_incorrect"));
        if (data.get("num_liet_wrong") != null) cv.put("num_liet_wrong", (Integer) data.get("num_liet_wrong"));
        if (data.get("device_info") != null) cv.put("device_info", (String) data.get("device_info"));
        db.insertWithOnConflict("exam_sessions", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }
}


