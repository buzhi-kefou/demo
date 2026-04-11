# Redis Demo

这个模块用于学习和测试 Redis 常用特性，以及 Redisson 的常用功能。

## 你可以练习的内容

- String / Hash / List / Set / ZSet
- Bitmap
- HyperLogLog
- Geo
- Redisson 分布式锁
- Redisson 原子计数器
- Redisson 限流器

## 启动前准备

- JDK 17
- Maven 3.8+
- 本地或可访问的 Redis

默认配置位于 `src/main/resources/application.yml`：

```yaml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    database: 0
```

## 启动

```bash
mvn -pl redis-demo spring-boot:run
```

应用默认运行在 `8088` 端口。

## 常用接口

```bash
curl -X GET http://localhost:8088/redis/ping
curl -X POST http://localhost:8088/redis/string/set -H "Content-Type: application/json" -d "{\"key\":\"hello\",\"value\":\"world\",\"ttlSeconds\":120}"
curl -X POST http://localhost:8088/redis/string/get -H "Content-Type: application/json" -d "{\"key\":\"hello\"}"
curl -X POST http://localhost:8088/redis/hash/put -H "Content-Type: application/json" -d "{\"key\":\"user:1\",\"values\":{\"name\":\"alice\",\"age\":18}}"
curl -X POST http://localhost:8088/redis/list/push -H "Content-Type: application/json" -d "{\"key\":\"list:1\",\"values\":[\"a\",\"b\",\"c\"]}"
curl -X POST http://localhost:8088/redis/set/add -H "Content-Type: application/json" -d "{\"key\":\"set:1\",\"values\":[\"a\",\"b\",\"c\"]}"
curl -X POST http://localhost:8088/redis/zset/add -H "Content-Type: application/json" -d "{\"key\":\"zset:1\",\"value\":\"tom\",\"score\":100}"
curl -X POST http://localhost:8088/redis/bitmap/set -H "Content-Type: application/json" -d "{\"key\":\"bitmap:1\",\"offset\":3,\"value\":true}"
curl -X POST http://localhost:8088/redis/hyperloglog/add -H "Content-Type: application/json" -d "{\"key\":\"hll:1\",\"values\":[\"u1\",\"u2\",\"u3\"]}"
curl -X POST http://localhost:8088/redis/geo/add -H "Content-Type: application/json" -d "{\"key\":\"geo:1\",\"member\":\"beijing\",\"longitude\":116.4074,\"latitude\":39.9042}"
curl -X POST http://localhost:8088/redis/redisson/lock -H "Content-Type: application/json" -d "{\"lockName\":\"lock:order:1\",\"waitSeconds\":3,\"leaseSeconds\":5}"
curl -X POST http://localhost:8088/redis/redisson/counter -H "Content-Type: application/json" -d "{\"key\":\"counter:1\"}"
curl -X POST http://localhost:8088/redis/redisson/rate-limit -H "Content-Type: application/json" -d "{\"rateLimiterName\":\"rl:1\",\"permitsPerSecond\":5}"
```
