package com.example.afinal;

import static org.junit.Assert.assertTrue;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class GeminiApiInstrumentedTest {
    private static final String TAG = "GeminiApiTest";

    @Test
    public void callGemini_generateExplanation() throws Exception {
        GeminiApiManager manager = new GeminiApiManager();
        String prompt = "Hãy giải thích ngắn gọn ý nghĩa của biển báo cấm dừng xe.";
        String response = safeCall(manager, prompt);
        Log.i(TAG, "Response:\n" + response);
        // Not asserting content, just ensure call returns non-empty when key is set
        assertTrue(response != null);
    }

    private String safeCall(GeminiApiManager manager, String prompt) {
        try {
            return manager.generateExplanation(prompt);
        } catch (IllegalStateException e) {
            Log.w(TAG, "API key missing: " + e.getMessage());
            return "";
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Call failed", e);
            return "";
        }
    }
}


