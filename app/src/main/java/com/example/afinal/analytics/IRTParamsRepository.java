package com.example.afinal.analytics;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository for caching IRT (Item Response Theory) parameters:
 * - b_item: question difficulty
 * - theta_user: user ability
 * - hlr_params: half-life regression parameters by topic
 */
public class IRTParamsRepository {
    private static final String TAG = "IRTParamsRepository";
    private static final String PREFS_NAME = "irt_params_cache";
    private static final String KEY_B_ITEM_PREFIX = "b_item_";
    private static final String KEY_THETA_USER = "theta_user";
    private static final String KEY_HLR_PARAMS = "hlr_params";
    private static final String KEY_LAST_SYNC = "last_sync_ms";

    private final SharedPreferences prefs;
    private final FirebaseFirestore db;

    public IRTParamsRepository(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Get b_item (difficulty) for a question, with caching
     */
    public double getBItem(String questionId) {
        String key = KEY_B_ITEM_PREFIX + questionId;
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(0.0)));
    }

    /**
     * Cache b_item for a question
     */
    public void setBItem(String questionId, double bItem) {
        String key = KEY_B_ITEM_PREFIX + questionId;
        prefs.edit().putLong(key, Double.doubleToLongBits(bItem)).apply();
    }

    /**
     * Get theta_user (user ability), with caching
     */
    public double getThetaUser(String userId) {
        String key = KEY_THETA_USER + "_" + userId;
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(0.0)));
    }

    /**
     * Cache theta_user for a user
     */
    public void setThetaUser(String userId, double theta) {
        String key = KEY_THETA_USER + "_" + userId;
        prefs.edit().putLong(key, Double.doubleToLongBits(theta)).apply();
    }

    /**
     * Get HLR half-life for a topic (in days)
     */
    public double getHLRHalfLife(int topicId) {
        String key = KEY_HLR_PARAMS + "_topic_" + topicId;
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(3.0))); // Default 3 days
    }

    /**
     * Cache HLR half-life for a topic
     */
    public void setHLRHalfLife(int topicId, double halfLifeDays) {
        String key = KEY_HLR_PARAMS + "_topic_" + topicId;
        prefs.edit().putLong(key, Double.doubleToLongBits(halfLifeDays)).apply();
    }

    /**
     * Sync IRT params from Firestore (if stored there)
     */
    public void syncFromFirestore(String userId) {
        // Sync user theta
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Map<String, Object> irtParams = (Map<String, Object>) document.get("irt_params");
                        if (irtParams != null) {
                            Double theta = (Double) irtParams.get("theta_user");
                            if (theta != null) {
                                setThetaUser(userId, theta);
                                Log.d(TAG, "Synced theta_user: " + theta);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error syncing user theta", e));

        // Sync HLR params (if stored in a global collection)
        db.collection("hlr_params").document("default")
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> hlrMap = (Map<String, Object>) document.get("H_topic");
                        if (hlrMap != null) {
                            for (Map.Entry<String, Object> entry : hlrMap.entrySet()) {
                                try {
                                    int topicId = Integer.parseInt(entry.getKey());
                                    double halfLife = ((Number) entry.getValue()).doubleValue();
                                    setHLRHalfLife(topicId, halfLife);
                                } catch (Exception e) {
                                    Log.w(TAG, "Error parsing HLR param for topic " + entry.getKey(), e);
                                }
                            }
                            Log.d(TAG, "Synced HLR params");
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error syncing HLR params", e));
    }

    /**
     * Update IRT params from API response metadata
     */
    public void updateFromRecommendationMetadata(String userId, Map<String, Object> metadata) {
        // Update theta_user if present
        @SuppressWarnings("unchecked")
        Map<String, Object> irtParams = (Map<String, Object>) metadata.get("irt_params");
        if (irtParams != null) {
            Double theta = (Double) irtParams.get("theta_user");
            if (theta != null) {
                setThetaUser(userId, theta);
            }
        }
    }

    /**
     * Get last sync timestamp
     */
    public long getLastSyncMs() {
        return prefs.getLong(KEY_LAST_SYNC, 0);
    }

    /**
     * Update last sync timestamp
     */
    public void updateLastSync() {
        prefs.edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply();
    }
}

