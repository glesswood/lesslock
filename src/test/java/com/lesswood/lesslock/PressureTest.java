package com.lesswood.lesslock;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PressureTest {
    @Test
    public void pressure() throws Exception {
        LockConfig cfg = LockConfig.builder().lease(Duration.ofSeconds(10)).autoRenew(true).build();
        try (RedisLockClient client = new RedisLockClient(cfg)) {
            String key = "press:key";
            AtomicInteger acq = new AtomicInteger();
            ExecutorService es = Executors.newFixedThreadPool(32);
            CountDownLatch latch = new CountDownLatch(500);
            for (int i = 0; i < 500; i++) {
                es.submit(() -> {
                    var lock = client.getLock(key, null);
                    if (lock.tryLock()) {
                        try (lock) { acq.incrementAndGet(); }
                    }
                    latch.countDown();
                });
            }
            latch.await(30, TimeUnit.SECONDS);
            es.shutdown();
            System.out.println("acquired=" + acq.get());
        }
    }
}