package com.example.afinal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiApiManager {
    private static final String DEFAULT_MODEL = "gemini-2.0-flash";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final String apiKey;
    private final String modelName;

    public GeminiApiManager() {
        this(BuildConfig.GEMINI_API_KEY, DEFAULT_MODEL);
    }

    public GeminiApiManager(String apiKey, String modelName) {
        this.httpClient = new OkHttpClient.Builder().build();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.modelName = (modelName == null || modelName.isEmpty()) ? DEFAULT_MODEL : modelName;
    }

    public String generateExplanation(String prompt) throws IOException, JSONException {
        if (apiKey.isEmpty()) {
            throw new IllegalStateException("GEMINI_API_KEY is empty. Add it to local.properties and rebuild.");
        }
        String endpoint = "https://generativelanguage.googleapis.com/v1/models/" + modelName + ":generateContent?key=" + apiKey;

        JSONObject textPart = new JSONObject();
        textPart.put("text", prompt);

        JSONObject partsObj = new JSONObject();
        // Newer API can accept parts as array of objects; keep text-only simple form
        // but conform to: { contents: [ { role: "user", parts: [ { text: "..." } ] } ] }

        JSONArray partsArray = new JSONArray();
        partsArray.put(textPart);

        JSONObject content = new JSONObject();
        content.put("role", "user");
        content.put("parts", partsArray);

        JSONArray contents = new JSONArray();
        contents.put(content);

        JSONObject requestBodyJson = new JSONObject();
        requestBodyJson.put("contents", contents);

        RequestBody body = RequestBody.create(requestBodyJson.toString().getBytes(StandardCharsets.UTF_8), JSON);
        Request request = new Request.Builder()
                .url(endpoint)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                System.err.println("Gemini API error HTTP " + response.code() + ": " + errorBody);
                throw new IOException("Gemini API call failed with code " + response.code());
            }
            String responseBody = response.body() != null ? response.body().string() : "";
            return extractText(responseBody);
        }
    }

    public String generateExplanationWithImage(String prompt, byte[] imageBytes, String mimeType) throws IOException, JSONException {
        if (apiKey.isEmpty()) {
            throw new IllegalStateException("GEMINI_API_KEY is empty. Add it to local.properties and rebuild.");
        }
        if (imageBytes == null || imageBytes.length == 0) {
            return generateExplanation(prompt);
        }
        String safeMime = (mimeType == null || mimeType.isEmpty()) ? "image/png" : mimeType;
        String endpoint = "https://generativelanguage.googleapis.com/v1/models/" + modelName + ":generateContent?key=" + apiKey;

        // Build parts: [ { inline_data: { mime_type, data } }, { text } ]
        JSONObject inlineData = new JSONObject();
        inlineData.put("mime_type", safeMime);
        inlineData.put("data", Base64.getEncoder().encodeToString(imageBytes));

        JSONObject imagePart = new JSONObject();
        imagePart.put("inline_data", inlineData);

        JSONObject textPart = new JSONObject();
        textPart.put("text", prompt);

        JSONArray partsArray = new JSONArray();
        partsArray.put(imagePart);
        partsArray.put(textPart);

        JSONObject content = new JSONObject();
        content.put("role", "user");
        content.put("parts", partsArray);

        JSONArray contents = new JSONArray();
        contents.put(content);

        JSONObject requestBodyJson = new JSONObject();
        requestBodyJson.put("contents", contents);

        RequestBody body = RequestBody.create(requestBodyJson.toString().getBytes(StandardCharsets.UTF_8), JSON);
        Request request = new Request.Builder()
                .url(endpoint)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                System.err.println("Gemini API error HTTP " + response.code() + ": " + errorBody);
                throw new IOException("Gemini API call failed with code " + response.code());
            }
            String responseBody = response.body() != null ? response.body().string() : "";
            return extractText(responseBody);
        }
    }

    private String extractText(String responseBody) throws JSONException {
        // Expected shape: { candidates: [ { content: { parts: [ { text: "..." }, ... ] } } ] }
        JSONObject root = new JSONObject(responseBody);
        JSONArray candidates = root.optJSONArray("candidates");
        if (candidates == null || candidates.length() == 0) {
            return "";
        }
        JSONObject first = candidates.getJSONObject(0);
        JSONObject content = first.optJSONObject("content");
        if (content == null) {
            return "";
        }
        JSONArray parts = content.optJSONArray("parts");
        if (parts == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length(); i++) {
            JSONObject part = parts.getJSONObject(i);
            String text = part.optString("text", "");
            if (!text.isEmpty()) {
                if (sb.length() > 0) sb.append('\n');
                sb.append(text);
            }
        }
        return sb.toString();
    }
}


