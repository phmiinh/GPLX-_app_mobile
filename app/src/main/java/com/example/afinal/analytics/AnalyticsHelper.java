package com.example.afinal.analytics;

import android.content.Context;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Map;

/**
 * Helper class for logging Firebase Analytics events
 */
public class AnalyticsHelper {
    private static final String TAG = "AnalyticsHelper";
    private static FirebaseAnalytics firebaseAnalytics;

    public static void initialize(Context context) {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context.getApplicationContext());
            Log.d(TAG, "Firebase Analytics initialized");
        }
    }

    public static FirebaseAnalytics getInstance(Context context) {
        if (firebaseAnalytics == null) {
            initialize(context);
        }
        return firebaseAnalytics;
    }

    /**
     * Log AI smart review opened event
     */
    public static void logAISmartReviewOpened(Context context) {
        FirebaseAnalytics analytics = getInstance(context);
        analytics.logEvent("ai_smart_review_opened", null);
        Log.d(TAG, "Logged: ai_smart_review_opened");
    }

    /**
     * Log AI due list viewed event
     */
    public static void logAIDueListViewViewed(Context context) {
        FirebaseAnalytics analytics = getInstance(context);
        analytics.logEvent("ai_due_list_viewed", null);
        Log.d(TAG, "Logged: ai_due_list_viewed");
    }

    /**
     * Log AI practice started event
     */
    public static void logAIPracticeStarted(Context context, String mode, Integer topicId, int numQuestions, boolean criticalOnly) {
        FirebaseAnalytics analytics = getInstance(context);
        android.os.Bundle params = new android.os.Bundle();
        params.putString("mode", mode);
        if (topicId != null) {
            params.putInt("topic_id", topicId);
        }
        params.putInt("num_questions", numQuestions);
        params.putBoolean("critical_only", criticalOnly);
        analytics.logEvent("ai_practice_started", params);
        Log.d(TAG, "Logged: ai_practice_started");
    }

    /**
     * Log AI recommendation clicked event
     */
    public static void logAIRecommendationClicked(Context context, String questionId, int topicId, boolean isCritical, double pCorrect) {
        FirebaseAnalytics analytics = getInstance(context);
        android.os.Bundle params = new android.os.Bundle();
        params.putString("question_id", questionId);
        params.putInt("topic_id", topicId);
        params.putBoolean("is_critical", isCritical);
        
        // Bucket p_correct
        String pCorrectBucket = "high";
        if (pCorrect < 0.4) {
            pCorrectBucket = "very_low";
        } else if (pCorrect < 0.6) {
            pCorrectBucket = "low";
        } else if (pCorrect < 0.8) {
            pCorrectBucket = "medium";
        }
        params.putString("p_correct_bucket", pCorrectBucket);
        
        analytics.logEvent("ai_recommendation_clicked", params);
        Log.d(TAG, "Logged: ai_recommendation_clicked");
    }

    /**
     * Log AI toggle prioritize weak changed event
     */
    public static void logAITogglePrioritizeWeakChanged(Context context, boolean enabled) {
        FirebaseAnalytics analytics = getInstance(context);
        android.os.Bundle params = new android.os.Bundle();
        params.putBoolean("prioritize_weak", enabled);
        analytics.logEvent("ai_toggle_prioritize_weak_changed", params);
        Log.d(TAG, "Logged: ai_toggle_prioritize_weak_changed");
    }

    /**
     * Log AI explanation opened event
     */
    public static void logAIExplanationOpened(Context context, String questionId, int topicId, boolean isCritical) {
        FirebaseAnalytics analytics = getInstance(context);
        android.os.Bundle params = new android.os.Bundle();
        params.putString("question_id", questionId);
        params.putInt("topic_id", topicId);
        params.putBoolean("is_critical", isCritical);
        analytics.logEvent("ai_explanation_opened", params);
        Log.d(TAG, "Logged: ai_explanation_opened");
    }

    /**
     * Log attempt logged event (for AI mode)
     */
    public static void logAttemptLogged(Context context, String mode, int topicId, boolean isCritical, double pCorrect, String dueBucket) {
        FirebaseAnalytics analytics = getInstance(context);
        android.os.Bundle params = new android.os.Bundle();
        params.putString("mode", mode);
        params.putInt("topic_id", topicId);
        params.putBoolean("is_critical", isCritical);
        
        String pCorrectBucket = "high";
        if (pCorrect < 0.4) {
            pCorrectBucket = "very_low";
        } else if (pCorrect < 0.6) {
            pCorrectBucket = "low";
        } else if (pCorrect < 0.8) {
            pCorrectBucket = "medium";
        }
        params.putString("p_correct_bucket", pCorrectBucket);
        
        if (dueBucket != null) {
            params.putString("due_bucket", dueBucket);
        }
        
        analytics.logEvent("attempt_logged", params);
        Log.d(TAG, "Logged: attempt_logged");
    }
}

