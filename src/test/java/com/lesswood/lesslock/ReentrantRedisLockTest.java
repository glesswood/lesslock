package com.lesswood.lesslock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class ReentrantRedisLockTest {

    @Test
    public void testReentrant() {
        LockConfig cfg = LockConfig.builder().lease(Duration.ofSeconds(5)).autoRenew(false).build();
        try (RedisLockClient client = new RedisLockClient(cfg)) {
            ReentrantRedisLock lock = client.getLock("test:key1", "owner-A");
            Assertions.assertTrue(lock.tryLock());
            Assertions.assertTrue(lock.tryLock());
            long left = lock.unlockAndGetLeftCount();
            Assertions.assertEquals(1L, left);
            long zero = lock.unlockAndGetLeftCount();
            Assertions.assertEquals(0L, zero);
        }
    }
}