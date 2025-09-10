-- KEYS[1] = lockKey
-- ARGV[1] = ownerId

if (redis.call('hexists', KEYS[1], ARGV[1]) == 0) then
    return -1 -- 非持有者/锁已过期
end

local cnt = redis.call('hincrby', KEYS[1], ARGV[1], -1)
if (cnt > 0) then
    return cnt -- 仍有重入层级
end

redis.call('hdel', KEYS[1], ARGV[1])
if (redis.call('hlen', KEYS[1]) == 0) then
    redis.call('del', KEYS[1])
end
return 0