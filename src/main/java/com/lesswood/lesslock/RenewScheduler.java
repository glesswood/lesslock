package com.lesswood.lesslock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;

class RenewScheduler implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(RenewScheduler.class);

    private final ScheduledExecutorService ses =
            Executors.newScheduledThreadPool(1, r -> { Thread t = new Thread(r, "lesslock-renew"); t.setDaemon(true); return t; });

    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();

    public void schedule(String lockKey, String ownerId, Duration interval, Runnable renewAction) {
        cancel(lockKey, ownerId);
        ScheduledFuture<?> f = ses.scheduleAtFixedRate(() -> {
            try { renewAction.run(); } catch (Throwable e) {
                log.warn("renew failed for {} by {}: {}", lockKey, ownerId, e.toString());
            }
        }, interval.toMillis(), interval.toMillis(), TimeUnit.MILLISECONDS);
        tasks.put(lockKey + "|" + ownerId, f);
    }

    public void cancel(String lockKey, String ownerId) {
        ScheduledFuture<?> f = tasks.remove(lockKey + "|" + ownerId);
        if (f != null) f.cancel(false);
    }

    @Override public void close() {
        tasks.values().forEach(f -> f.cancel(false));
        ses.shutdownNow();
    }
}