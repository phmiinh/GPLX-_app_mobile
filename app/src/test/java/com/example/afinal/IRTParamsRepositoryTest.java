package com.example.afinal;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.afinal.analytics.IRTParamsRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;

/**
 * Unit tests for IRTParamsRepository
 */
@RunWith(RobolectricTestRunner.class)
public class IRTParamsRepositoryTest {

    private Context context;
    private IRTParamsRepository repository;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        repository = new IRTParamsRepository(context);
    }

    @Test
    public void testRepositoryInitialization() {
        assertNotNull(repository);
    }

    @Test
    public void testBItemStorage() {
        String questionId = "123";
        double bItem = 0.75;

        repository.setBItem(questionId, bItem);
        double retrieved = repository.getBItem(questionId);

        assertEquals(bItem, retrieved, 0.001);
    }

    @Test
    public void testBItemDefaultValue() {
        String questionId = "nonexistent";
        double defaultValue = repository.getBItem(questionId);

        assertEquals(0.0, defaultValue, 0.001);
    }

    @Test
    public void testThetaUserStorage() {
        String userId = "user123";
        double theta = 1.25;

        repository.setThetaUser(userId, theta);
        double retrieved = repository.getThetaUser(userId);

        assertEquals(theta, retrieved, 0.001);
    }

    @Test
    public void testHLRHalfLifeStorage() {
        int topicId = 1;
        double halfLife = 5.5;

        repository.setHLRHalfLife(topicId, halfLife);
        double retrieved = repository.getHLRHalfLife(topicId);

        assertEquals(halfLife, retrieved, 0.001);
    }

    @Test
    public void testHLRHalfLifeDefaultValue() {
        int topicId = 999;
        double defaultValue = repository.getHLRHalfLife(topicId);

        assertEquals(3.0, defaultValue, 0.001); // Default 3 days
    }

    @Test
    public void testLastSyncTimestamp() {
        long now = System.currentTimeMillis();
        repository.updateLastSync();
        long lastSync = repository.getLastSyncMs();

        assertTrue(lastSync > 0);
        assertTrue(lastSync <= now + 1000); // Allow 1 second tolerance
    }
}

