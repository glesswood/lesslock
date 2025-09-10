# LessLock：基于 Redis + Lua 的分布式锁（可重入 + 续约）

- 原子 tryLock/unlock/renew（Lua）
- 可重入（同 ownerId 计数递增）
- 自动续约（默认 lease/3 周期）
- 线程池压测示例

## 快速开始
1) 起 Redis：`docker run -p 6379:6379 -d redis:6`
2) `mvn -q -DskipTests test` 运行测试
3) 打包：`mvn -q -DskipTests install`