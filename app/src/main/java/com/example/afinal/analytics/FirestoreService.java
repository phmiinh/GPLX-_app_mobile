package com.example.afinal.analytics;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirestoreService {
    private final FirebaseFirestore db;

    public FirestoreService() {
        this.db = FirebaseFirestore.getInstance();
    }

    public Task<DocumentReference> saveAttempt(Map<String, Object> data) {
        return db.collection("attempts").add(new HashMap<>(data));
    }

    public Task<DocumentReference> saveExamSession(Map<String, Object> data) {
        return db.collection("exam_sessions").add(new HashMap<>(data));
    }

    public Task<Void> upsertQuestionMeta(String questionId, Map<String, Object> data) {
        return db.collection("question_meta").document(questionId).set(new HashMap<>(data));
    }
}


