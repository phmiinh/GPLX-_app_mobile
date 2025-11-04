package com.example.afinal.analytics;

import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirestoreService {
    private static final String TAG = "FirestoreService";
    private final FirebaseFirestore db;

    public FirestoreService() {
        this.db = FirebaseFirestore.getInstance();
        Log.d(TAG, "FirestoreService initialized");
    }

    public Task<DocumentReference> saveAttempt(Map<String, Object> data) {
        Log.d(TAG, "Saving attempt to Firestore: " + data);
        Task<DocumentReference> task = db.collection("attempts").add(new HashMap<>(data));
        task.addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(TAG, "Attempt saved successfully with ID: " + documentReference.getId());
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error saving attempt to Firestore", e);
            }
        });
        return task;
    }

    public Task<DocumentReference> saveExamSession(Map<String, Object> data) {
        Log.d(TAG, "Saving exam session to Firestore: " + data);
        Task<DocumentReference> task = db.collection("exam_sessions").add(new HashMap<>(data));
        task.addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(TAG, "Exam session saved successfully with ID: " + documentReference.getId());
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error saving exam session to Firestore", e);
            }
        });
        return task;
    }

    public Task<Void> upsertQuestionMeta(String questionId, Map<String, Object> data) {
        Log.d(TAG, "Upserting question meta for questionId: " + questionId);
        Task<Void> task = db.collection("question_meta").document(questionId).set(new HashMap<>(data));
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Question meta upserted successfully for questionId: " + questionId);
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error upserting question meta for questionId: " + questionId, e);
            }
        });
        return task;
    }

    public Task<DocumentReference> saveBookmark(Map<String, Object> data) {
        Log.d(TAG, "Saving bookmark to Firestore: " + data);
        Task<DocumentReference> task = db.collection("bookmarks").add(new HashMap<>(data));
        task.addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(TAG, "Bookmark saved successfully with ID: " + documentReference.getId());
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error saving bookmark to Firestore", e);
            }
        });
        return task;
    }
}


