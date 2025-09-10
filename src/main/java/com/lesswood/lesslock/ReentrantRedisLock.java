package com.lesswood.lesslock;

import com.lesswood.lesslock.util.Ids;

import java.time.Duration;
import java.util.Objects;

public class ReentrantRedisLock implements LockHandle {
    private final RedisLockClient client;
    private final String bizKey;
    private final String ownerId;

    ReentrantRedisLock(RedisLockClient client, String bizKey, String ownerId) {
        this.client = Objects.requireNonNull(client);
        this.bizKey = Objects.requireNonNull(bizKey);
        this.ownerId = (ownerId == null ? Ids.newOwnerId() : ownerId);
    }

    public boolean tryLock() { return client.tryLock(bizKey, ownerId); }

    public boolean tryLock(Duration lease) { return client.tryLock(bizKey, ownerId, lease); }

    public long unlockAndGetLeftCount() { return client.unlock(bizKey, ownerId); }

    @Override public String key() { return bizKey; }

    @Override public String ownerId() { return ownerId; }

    @Override public void unlock() { client.unlock(bizKey, ownerId); }
}