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
        
        // Convert data to Firestore-compatible format
        Map<String, Object> firestoreData = convertToFirestoreFormat(data);
        
        Task<DocumentReference> task = db.collection("exam_sessions").add(firestoreData);
        task.addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(TAG, "Exam session saved successfully with ID: " + documentReference.getId());
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error saving exam session to Firestore: " + e.getMessage(), e);
                // Log full stack trace for debugging
                e.printStackTrace();
            }
        });
        return task;
    }

    public Task<DocumentReference> savePracticeSession(Map<String, Object> data) {
        Log.d(TAG, "Saving practice session to Firestore: " + data);
        
        // Convert data to Firestore-compatible format
        Map<String, Object> firestoreData = convertToFirestoreFormat(data);
        
        Task<DocumentReference> task = db.collection("practice_sessions").add(firestoreData);
        task.addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(TAG, "Practice session saved successfully with ID: " + documentReference.getId());
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error saving practice session to Firestore: " + e.getMessage(), e);
                e.printStackTrace();
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

    /**
     * Upsert basic user profile for leaderboard / personalization.
     * Data map is merged into existing document if present.
     */
    public Task<Void> upsertUser(String userId, Map<String, Object> data) {
        Log.d(TAG, "Upserting user profile for userId: " + userId + " data=" + data);
        Task<Void> task = db.collection("users").document(userId).set(new HashMap<>(data), com.google.firebase.firestore.SetOptions.merge());
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "User profile upserted successfully for userId: " + userId);
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error upserting user profile for userId: " + userId, e);
            }
        });
        return task;
    }
    
    /**
     * Convert Map data to Firestore-compatible format:
     * - Convert Integer to Long (Firestore prefers Long for numbers)
     * - Ensure HashMap values are properly serialized
     * - Handle nested maps recursively
     */
    private Map<String, Object> convertToFirestoreFormat(Map<String, Object> data) {
        Map<String, Object> converted = new HashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value == null) {
                converted.put(key, null);
            } else if (value instanceof Integer) {
                // Convert Integer to Long for Firestore compatibility
                converted.put(key, ((Integer) value).longValue());
            } else if (value instanceof HashMap) {
                // Recursively convert nested HashMaps
                @SuppressWarnings("unchecked")
                HashMap<String, Object> nestedMap = (HashMap<String, Object>) value;
                Map<String, Object> convertedNested = new HashMap<>();
                for (Map.Entry<String, Object> nestedEntry : nestedMap.entrySet()) {
                    Object nestedValue = nestedEntry.getValue();
                    if (nestedValue instanceof Integer) {
                        convertedNested.put(nestedEntry.getKey(), ((Integer) nestedValue).longValue());
                    } else {
                        convertedNested.put(nestedEntry.getKey(), nestedValue);
                    }
                }
                converted.put(key, convertedNested);
            } else {
                // Keep other types as-is (String, Long, Double, Boolean, etc.)
                converted.put(key, value);
            }
        }
        return converted;
    }

    /**
     * Upsert IRT parameters for a question (b_item, a_item)
     */
    public Task<Void> upsertIRTParams(String questionId, Map<String, Object> data) {
        Log.d(TAG, "Upserting IRT params for questionId: " + questionId);
        Task<Void> task = db.collection("irt_params").document(questionId).set(new HashMap<>(data), com.google.firebase.firestore.SetOptions.merge());
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "IRT params upserted successfully for questionId: " + questionId);
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error upserting IRT params for questionId: " + questionId, e);
            }
        });
        return task;
    }

    /**
     * Upsert user IRT parameters (theta_user)
     */
    public Task<Void> upsertUserIRTParams(String userId, double thetaUser) {
        Log.d(TAG, "Upserting user IRT params for userId: " + userId);
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> irtParams = new HashMap<>();
        irtParams.put("theta_user", thetaUser);
        irtParams.put("last_updated_ms", System.currentTimeMillis());
        data.put("irt_params", irtParams);
        
        Task<Void> task = db.collection("users").document(userId).set(data, com.google.firebase.firestore.SetOptions.merge());
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "User IRT params upserted successfully for userId: " + userId);
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error upserting user IRT params for userId: " + userId, e);
            }
        });
        return task;
    }
}


