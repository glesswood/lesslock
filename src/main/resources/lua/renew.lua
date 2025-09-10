-- KEYS[1] = lockKey
-- ARGV[1] = ownerId
-- ARGV[2] = ttlMillis

if (redis.call('hexists', KEYS[1], ARGV[1]) == 1) then
    return redis.call('pexpire', KEYS[1], ARGV[2])
end
return 0