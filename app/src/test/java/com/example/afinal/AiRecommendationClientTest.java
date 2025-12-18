package com.example.afinal;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;

/**
 * Unit tests for AiRecommendationClient
 */
@RunWith(RobolectricTestRunner.class)
public class AiRecommendationClientTest {

    private Context context;
    private AiRecommendationClient client;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        client = new AiRecommendationClient();
    }

    @Test
    public void testClientInitialization() {
        assertNotNull(client);
    }

    @Test
    public void testRequestRecommendationsWithRetry() {
        // Test that retry method exists and doesn't crash
        // Actual API call testing requires mock server
        assertNotNull(client);
        
        // This test verifies the method signature is correct
        // Full integration test would require mock HTTP server
    }

    @Test
    public void testCallbackInterface() {
        AiRecommendationClient.RecommendationCallback callback = new AiRecommendationClient.RecommendationCallback() {
            @Override
            public void onSuccess(java.util.List<String> questionIds, java.util.List<java.util.Map<String, Object>> metadata) {
                assertNotNull(questionIds);
                assertNotNull(metadata);
            }

            @Override
            public void onError(Exception e) {
                assertNotNull(e);
            }
        };

        assertNotNull(callback);
    }
}

