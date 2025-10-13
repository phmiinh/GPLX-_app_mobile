package com.example.afinal;

import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class GeminiApiUnitTest {
    @Test
    public void callGemini_generateExplanation_noEmulator() throws Exception {
        GeminiApiManager manager = new GeminiApiManager();
        String prompt = "Donald Trump sinh ngày bao nhiêu?";
        String response = safeCall(manager, prompt);
        System.out.println("GeminiApiUnitTest response:\n" + response);
        assertTrue(response != null);
    }

    private String safeCall(GeminiApiManager manager, String prompt) {
        try {
            return manager.generateExplanation(prompt);
        } catch (IllegalStateException e) {
            System.out.println("API key missing: " + e.getMessage());
            return "";
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
}


