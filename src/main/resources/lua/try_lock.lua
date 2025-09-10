-- KEYS[1] = lockKey
-- ARGV[1] = ownerId
-- ARGV[2] = ttlMillis

if (redis.call('exists', KEYS[1]) == 0) then
    redis.call('hset', KEYS[1], ARGV[1], 1)
    redis.call('pexpire', KEYS[1], ARGV[2])
    return 1
end

-- 可重入：同 ownerId 计数+1 并续期
if (redis.call('hexists', KEYS[1], ARGV[1]) == 1) then
    redis.call('hincrby', KEYS[1], ARGV[1], 1)
    redis.call('pexpire', KEYS[1], ARGV[2])
    return 1
end

return 0