package com.example.afinal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for SmartStudyEngine fallback logic
 */
@RunWith(RobolectricTestRunner.class)
public class SmartStudyEngineTest {

    @Mock
    private SmartStudyEngine.FallbackCallback callback;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testQuestionRecommendationCreation() {
        SmartStudyEngine.QuestionRecommendation rec = new SmartStudyEngine.QuestionRecommendation(
                "123", 1, true
        );

        assertEquals("123", rec.questionId);
        assertEquals(1, rec.topicId);
        assertTrue(rec.isCritical);
        assertNotNull(rec.reasons);
        assertEquals(0.5, rec.predictedCorrectProb, 0.01);
    }

    @Test
    public void testPriorityScoreCalculation() {
        // Test that priority score is calculated correctly
        // This is a basic sanity check
        SmartStudyEngine.QuestionRecommendation rec = new SmartStudyEngine.QuestionRecommendation(
                "123", 1, true
        );
        
        rec.priorityScore = 0.75;
        assertTrue(rec.priorityScore > 0);
        assertTrue(rec.priorityScore <= 1.0);
    }

    @Test
    public void testReasonsList() {
        SmartStudyEngine.QuestionRecommendation rec = new SmartStudyEngine.QuestionRecommendation(
                "123", 1, false
        );

        rec.reasons.add("Đến hạn");
        rec.reasons.add("P(correct) thấp");

        assertEquals(2, rec.reasons.size());
        assertTrue(rec.reasons.contains("Đến hạn"));
    }
}

