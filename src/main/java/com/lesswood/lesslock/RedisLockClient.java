package com.lesswood.lesslock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class RedisLockClient implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(RedisLockClient.class);

    private final LockConfig config;
    private final JedisPool pool;
    private final String tryLockSha;
    private final String unlockSha;
    private final String renewSha;
    private final RenewScheduler renewScheduler = new RenewScheduler();

    public RedisLockClient(LockConfig config) {
        this.config = Objects.requireNonNull(config);
        JedisPoolConfig pc = new JedisPoolConfig();
        pc.setMaxTotal(64); pc.setMaxIdle(16); pc.setMinIdle(2);
        this.pool = new JedisPool(pc, config.redisHost(), config.redisPort(), 2000,
                config.redisPassword(), config.database(), null);
        try (Jedis j = pool.getResource()) {
            tryLockSha = j.scriptLoad(loadLua("/lua/try_lock.lua"));
            unlockSha = j.scriptLoad(loadLua("/lua/unlock.lua"));
            renewSha  = j.scriptLoad(loadLua("/lua/renew.lua"));
        }
    }

    private String loadLua(String path) {
        try {
            byte[] bytes = RedisLockClient.class.getResourceAsStream(path).readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new LockException("load lua failed: " + path, e);
        }
    }

    private String lockKey(String bizKey) { return config.keyPrefix() + bizKey; }

    /** 非阻塞尝试加锁（含可重入） */
    public boolean tryLock(String bizKey, String ownerId) {
        return tryLock(bizKey, ownerId, config.lease());
    }

    public boolean tryLock(String bizKey, String ownerId, Duration lease) {
        String key = lockKey(bizKey);
        List<String> keys = List.of(key);
        List<String> argv = List.of(ownerId, String.valueOf(lease.toMillis()));
        try (Jedis j = pool.getResource()) {
            Object r = j.evalsha(tryLockSha, keys, argv);
            boolean ok = Long.valueOf(1L).equals(r);
            if (ok && config.autoRenew()) {
                renewScheduler.schedule(key, ownerId, config.renewInterval(), () -> renew(bizKey, ownerId, lease));
            }
            return ok;
        }
    }

    /** 解锁：返回剩余重入层级（0=完全释放；-1=非owner/已过期） */
    public long unlock(String bizKey, String ownerId) {
        String key = lockKey(bizKey);
        List<String> keys = List.of(key);
        List<String> argv = List.of(ownerId);
        try (Jedis j = pool.getResource()) {
            Object r = j.evalsha(unlockSha, keys, argv);
            long left = ((Number) r).longValue();
            if (left <= 0) renewScheduler.cancel(key, ownerId);
            return left;
        }
    }

    /** 续约：返回 1=成功，0=失败 */
    public long renew(String bizKey, String ownerId, Duration lease) {
        String key = lockKey(bizKey);
        List<String> keys = List.of(key);
        List<String> argv = List.of(ownerId, String.valueOf(lease.toMillis()));
        try (Jedis j = pool.getResource()) {
            Object r = j.evalsha(renewSha, keys, argv);
            return ((Number) r).longValue();
        }
    }

    /** 获取可重入锁对象（便于 try-with-resources） */
    public ReentrantRedisLock getLock(String bizKey, String ownerId) {
        return new ReentrantRedisLock(this, bizKey, ownerId);
    }

    @Override public void close() {
        try { renewScheduler.close(); } catch (Exception ignore) {}
        pool.close();
    }
}