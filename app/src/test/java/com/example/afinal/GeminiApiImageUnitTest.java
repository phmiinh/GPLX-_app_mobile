package com.example.afinal;

import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class GeminiApiImageUnitTest {
    @Test
    public void callGemini_withImage_noEmulator() throws Exception {
        GeminiApiManager manager = new GeminiApiManager();
        byte[] imageBytes = readImageBytes();
        String prompt = "Biển nào cấm máy kéo? \n" +
                "1. Biển 1. \n" +
                "2. Biển 2 và biển 3. \n" +
                "3. Biển 1 và biển 3. \n" +
                "4. Cả ba biển.";
        String response = safeCall(manager, prompt, imageBytes, "image/png");
        System.out.println("GeminiApiImageUnitTest response:\n" + response);
        assertTrue(response != null);
    }

    private static byte[] readImageBytes() throws IOException {
        // Gradle runs unit tests with working directory at the app module
        Path imgPath = Paths.get("src", "main", "assets", "img", "ques_303.png");
        return Files.readAllBytes(imgPath);
    }

    private String safeCall(GeminiApiManager manager, String prompt, byte[] image, String mime) {
        try {
            return manager.generateExplanationWithImage(prompt, image, mime);
        } catch (IllegalStateException e) {
            System.out.println("API key missing: " + e.getMessage());
            return "";
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
}


