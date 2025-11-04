package com.example.afinal.analytics;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreService {

    private static final String TAG = "FirestoreService";
    private final FirebaseFirestore db;

    public FirestoreService() {
        db = FirebaseFirestore.getInstance();
        Log.d(TAG, "FirestoreService initialized");
    }

    // ---------------------- INTERFACES ----------------------

    public interface BookmarkListListener {
        void onResult(List<Integer> questionIds);
        void onError(Exception e);
    }

    public interface BookmarkCheckListener {
        void onResult(boolean isBookmarked);
        void onError(Exception e);
    }


    // ---------------------- LẤY DANH SÁCH BOOKMARK ----------------------

    public void getBookmarkList(@NonNull String userId, @NonNull BookmarkListListener listener) {
        db.collection("bookmarks")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Integer> questionIds = new ArrayList<>();

                    if (documentSnapshot.exists()) {
                        Object obj = documentSnapshot.get("questionIds");
                        if (obj instanceof List<?>) {
                            for (Object item : (List<?>) obj) {
                                if (item instanceof Number) {
                                    questionIds.add(((Number) item).intValue());
                                }
                            }
                        }
                    }

                    listener.onResult(questionIds);
                })
                .addOnFailureListener(listener::onError);
    }

    // ---------------------- KIỂM TRA BOOKMARK ----------------------

    public void isQuestionBookmarked(@NonNull String userId, int questionId, @NonNull BookmarkCheckListener listener) {
        db.collection("bookmarks")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean exists = false;

                    if (documentSnapshot.exists()) {
                        Object obj = documentSnapshot.get("questionIds");
                        if (obj instanceof List<?>) {
                            List<?> list = (List<?>) obj;
                            exists = list.contains((long) questionId);
                        }
                    }

                    listener.onResult(exists);
                })
                .addOnFailureListener(listener::onError);
    }

    // ---------------------- THÊM BOOKMARK ----------------------

    public void addBookmark(@NonNull String userId, int questionId,
                            @NonNull OnSuccessListener<Void> onSuccess,
                            @NonNull OnFailureListener onFailure) {
        db.collection("bookmarks").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Long> listLong = new ArrayList<>();

                    if (documentSnapshot.exists()) {
                        Object obj = documentSnapshot.get("questionIds");
                        if (obj instanceof List<?>) {
                            for (Object item : (List<?>) obj) {
                                if (item instanceof Number) {
                                    listLong.add(((Number) item).longValue());
                                }
                            }
                        }
                    }

                    if (!listLong.contains((long) questionId)) {
                        listLong.add((long) questionId);
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("questionIds", listLong);

                    db.collection("bookmarks").document(userId)
                            .set(data)
                            .addOnSuccessListener(onSuccess)
                            .addOnFailureListener(onFailure);
                })
                .addOnFailureListener(onFailure);
    }

    // ---------------------- XÓA BOOKMARK ----------------------

    public void removeBookmark(@NonNull String userId, int questionId,
                               @NonNull OnSuccessListener<Void> onSuccess,
                               @NonNull OnFailureListener onFailure) {
        db.collection("bookmarks").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Object obj = documentSnapshot.get("questionIds");

                        if (obj instanceof List<?>) {
                            List<Long> listLong = new ArrayList<>();
                            for (Object item : (List<?>) obj) {
                                if (item instanceof Number) {
                                    listLong.add(((Number) item).longValue());
                                }
                            }

                            if (listLong.contains((long) questionId)) {
                                listLong.remove((Long) (long) questionId);

                                db.collection("bookmarks").document(userId)
                                        .update("questionIds", listLong)
                                        .addOnSuccessListener(onSuccess)
                                        .addOnFailureListener(onFailure);
                            } else {
                                onSuccess.onSuccess(null); // không có gì để xóa
                            }
                        } else {
                            onSuccess.onSuccess(null);
                        }
                    } else {
                        onSuccess.onSuccess(null);
                    }
                })
                .addOnFailureListener(onFailure);
    }

    // ---------------------- LƯU ATTEMPT ----------------------

    public void saveAttempt(@NonNull Map<String, Object> data) {
        db.collection("attempts")
                .add(new HashMap<>(data))
                .addOnSuccessListener(docRef ->
                        Log.d(TAG, "Attempt saved successfully with ID: " + docRef.getId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error saving attempt", e));
    }

    // ---------------------- LƯU SESSION ----------------------

    public void saveExamSession(@NonNull Map<String, Object> data) {
        db.collection("exam_sessions")
                .add(new HashMap<>(data))
                .addOnSuccessListener(docRef ->
                        Log.d(TAG, "Exam session saved with ID: " + docRef.getId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error saving exam session", e));
    }

    // ---------------------- UPSERT QUESTION META ----------------------

    public void upsertQuestionMeta(@NonNull String questionId, @NonNull Map<String, Object> data) {
        db.collection("question_meta")
                .document(questionId)
                .set(new HashMap<>(data))
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Question meta updated for questionId: " + questionId))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error upserting question meta", e));
    }

}
