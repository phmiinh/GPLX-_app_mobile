package com.example.afinal;

import android.content.Context;
import android.util.Log;

import com.example.afinal.analytics.UserIdentity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Lightweight HTTP client for the personalized recommendation API.
 *
 * The base URL is injected via BuildConfig.AI_API_BASE_URL so that
 * dev/staging/prod environments can be configured independently.
 */
public class AiRecommendationClient {

    public interface RecommendationCallback {
        void onSuccess(List<String> questionIds, List<Map<String, Object>> metadata);
        void onError(Exception e);
    }

    private static final String TAG = "AiRecommendationClient";
    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    private static final int DEFAULT_TIMEOUT_SECONDS = 10;
    private static final int MAX_RETRIES = 2;

    private final OkHttpClient client;
    private final String baseUrl;

    public AiRecommendationClient() {
        OkHttpClient defaultClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.client = defaultClient;
        this.baseUrl = BuildConfig.AI_API_BASE_URL;
        Log.d(TAG, "AiRecommendationClient initialized with baseUrl: " + this.baseUrl);
    }

    public AiRecommendationClient(OkHttpClient client, String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl != null ? baseUrl.trim() : "";
    }

    /**
     * Request recommendations with retry logic and exponential backoff
     */
    public void requestRecommendationsWithRetry(
            Context context,
            String mode,
            Integer topicId,
            int numQuestions,
            boolean criticalOnly,
            RecommendationCallback callback
    ) {
        requestRecommendationsWithRetry(context, mode, topicId, numQuestions, criticalOnly, callback, 0);
    }

    private void requestRecommendationsWithRetry(
            Context context,
            String mode,
            Integer topicId,
            int numQuestions,
            boolean criticalOnly,
            RecommendationCallback callback,
            int retryCount
    ) {
        requestRecommendations(context, mode, topicId, numQuestions, criticalOnly, new RecommendationCallback() {
            @Override
            public void onSuccess(List<String> questionIds, List<Map<String, Object>> metadata) {
                callback.onSuccess(questionIds, metadata);
            }

            @Override
            public void onError(Exception e) {
                if (retryCount < MAX_RETRIES && (e instanceof IOException || (e.getMessage() != null && e.getMessage().contains("timeout")))) {
                    // Exponential backoff: wait 2^retryCount seconds
                    long delayMs = (long) Math.pow(2, retryCount) * 1000;
                    Log.d(TAG, "Retrying request after " + delayMs + "ms (attempt " + (retryCount + 1) + "/" + MAX_RETRIES + ")");
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        requestRecommendationsWithRetry(context, mode, topicId, numQuestions, criticalOnly, callback, retryCount + 1);
                    }, delayMs);
                } else {
                    callback.onError(e);
                }
            }
        });
    }

    public void requestRecommendations(
            Context context,
            String mode,
            Integer topicId,
            int numQuestions,
            boolean criticalOnly,
            RecommendationCallback callback
    ) {
        Log.d(TAG, "requestRecommendations called - baseUrl: " + baseUrl + ", mode: " + mode + ", topicId: " + topicId);
        
        if (baseUrl.isEmpty()) {
            Log.e(TAG, "AI_API_BASE_URL is empty! Falling back to heuristic.");
            callback.onError(new IllegalStateException("AI_API_BASE_URL is empty. Configure it in local.properties or BuildConfig."));
            return;
        }

        String userId = UserIdentity.getUserId(context);
        Log.d(TAG, "User ID: " + userId);

        try {
            JSONObject body = new JSONObject();
            JSONObject ctx = new JSONObject();
            JSONObject filters = new JSONObject();

            body.put("user_id", userId);
            body.put("mode", mode);

            ctx.put("num_questions", numQuestions);
            ctx.put("time_of_day_bucket", "sang");
            ctx.put("current_timestamp_ms", System.currentTimeMillis());
            if (topicId != null) {
                ctx.put("topic_id", topicId);
            }
            body.put("context", ctx);

            if (criticalOnly) {
                filters.put("include_critical_only", true);
            }
            body.put("filters", filters);

            final String url = baseUrl + "/api/v1/recommendations";
            Log.d(TAG, "Making API request to: " + url);
            Log.d(TAG, "Request body: " + body.toString());
            
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(body.toString(), JSON))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Recommendation API call failed: " + e.getMessage(), e);
                    Log.e(TAG, "URL was: " + url);
                    callback.onError(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(TAG, "API response received: " + response.code());
                    if (!response.isSuccessful()) {
                        String body = response.body() != null ? response.body().string() : "";
                        Log.e(TAG, "Recommendation API error: " + response.code() + " body=" + body);
                        callback.onError(new IOException("Unexpected HTTP " + response.code()));
                        return;
                    }
                    String respBody = response.body() != null ? response.body().string() : "";
                    Log.d(TAG, "API response body length: " + respBody.length());
                    try {
                        JSONObject root = new JSONObject(respBody);
                        JSONObject data = root.getJSONObject("data");
                        JSONArray recs = data.getJSONArray("recommendations");

                        List<String> ids = new ArrayList<>();
                        List<Map<String, Object>> metaList = new ArrayList<>();
                        for (int i = 0; i < recs.length(); i++) {
                            JSONObject rec = recs.getJSONObject(i);
                            String qid = rec.optString("question_id", null);
                            if (qid == null) continue;
                            ids.add(qid);

                            Map<String, Object> meta = new HashMap<>();
                            meta.put("topic_id", rec.optInt("topic_id", 0));
                            meta.put("is_critical", rec.optBoolean("is_critical", false));
                            meta.put("has_image", rec.optBoolean("has_image", false));
                            meta.put("priority_score", rec.optDouble("priority_score", 0.0));
                            meta.put("predicted_correct_prob", rec.optDouble("predicted_correct_prob", 0.0));
                            meta.put("urgency_score", rec.optDouble("urgency_score", 0.0));
                            meta.put("due_time_ms", rec.optLong("due_time_ms", 0L));
                            meta.put("days_overdue", rec.optDouble("days_overdue", 0.0));
                            metaList.add(meta);
                        }
                        callback.onSuccess(ids, metaList);
                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to parse recommendation response", e);
                        callback.onError(e);
                    }
                }
            });
        } catch (JSONException e) {
            callback.onError(e);
        }
    }
}


