package com.parallel.learn.redis.web;

import com.parallel.learn.redis.config.RedisDemoProperties;
import com.parallel.learn.redis.service.RedisDemoService;
import com.parallel.learn.redis.service.RedissonDemoService;
import com.parallel.learn.redis.web.dto.BitRequest;
import com.parallel.learn.redis.web.dto.GeoRequest;
import com.parallel.learn.redis.web.dto.GeoDistanceRequest;
import com.parallel.learn.redis.web.dto.HashRequest;
import com.parallel.learn.redis.web.dto.HyperLogLogRequest;
import com.parallel.learn.redis.web.dto.KeyRequest;
import com.parallel.learn.redis.web.dto.ListRequest;
import com.parallel.learn.redis.web.dto.LockRequest;
import com.parallel.learn.redis.web.dto.RateLimitRequest;
import com.parallel.learn.redis.web.dto.StringValueRequest;
import com.parallel.learn.redis.web.dto.ZSetRequest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redis")
public class RedisDemoController {

    private final RedisDemoService redisDemoService;
    private final RedissonDemoService redissonDemoService;
    private final RedisDemoProperties properties;

    public RedisDemoController(RedisDemoService redisDemoService,
                               RedissonDemoService redissonDemoService,
                               RedisDemoProperties properties) {
        this.redisDemoService = redisDemoService;
        this.redissonDemoService = redissonDemoService;
        this.properties = properties;
    }

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return mapOf("module", "redis-demo", "keyPrefix", properties.getKeyPrefix(), "defaultTtlSeconds", properties.getDefaultTtlSeconds());
    }

    @PostMapping("/string/set")
    public ResponseEntity<Void> setString(@RequestBody StringValueRequest request) {
        long ttl = request.ttlSeconds() == null ? properties.getDefaultTtlSeconds() : request.ttlSeconds();
        redisDemoService.setString(request.key(), request.value(), Duration.ofSeconds(ttl));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/string/get")
    public Map<String, Object> getString(@RequestBody KeyRequest request) {
        return mapOf("value", redisDemoService.getString(request.key()));
    }

    @PostMapping("/hash/put")
    public ResponseEntity<Void> putHash(@RequestBody HashRequest request) {
        redisDemoService.putHash(request.key(), request.values());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/hash/get")
    public Map<String, Object> getHash(@RequestBody KeyRequest request) {
        return mapOf("value", redisDemoService.getHash(request.key()));
    }

    @PostMapping("/list/push")
    public Map<String, Object> pushList(@RequestBody ListRequest request) {
        return mapOf("size", redisDemoService.addToList(request.key(), request.values()));
    }

    @PostMapping("/list/range")
    public Map<String, Object> rangeList(@RequestBody KeyRequest request) {
        return mapOf("value", redisDemoService.getList(request.key(), 0, -1));
    }

    @PostMapping("/set/add")
    public Map<String, Object> addSet(@RequestBody ListRequest request) {
        return mapOf("size", redisDemoService.addToSet(request.key(), request.values()));
    }

    @PostMapping("/set/count")
    public Map<String, Object> countSet(@RequestBody KeyRequest request) {
        return mapOf("size", redisDemoService.countSetMembers(request.key()));
    }

    @PostMapping("/zset/add")
    public Map<String, Object> addZSet(@RequestBody ZSetRequest request) {
        return mapOf("size", redisDemoService.addToZSet(request.key(), request.value(), request.score()));
    }

    @PostMapping("/zset/top")
    public Map<String, Object> topZSet(@RequestBody KeyRequest request) {
        return mapOf("value", redisDemoService.getTopZSet(request.key(), 0, -1));
    }

    @PostMapping("/bitmap/set")
    public Map<String, Object> setBit(@RequestBody BitRequest request) {
        return mapOf("oldValue", redisDemoService.setBit(request.key(), request.offset(), request.value()));
    }

    @PostMapping("/bitmap/get")
    public Map<String, Object> getBit(@RequestBody BitRequest request) {
        return mapOf("value", redisDemoService.getBit(request.key(), request.offset()));
    }

    @PostMapping("/bitmap/count")
    public Map<String, Object> countBit(@RequestBody KeyRequest request) {
        return mapOf("count", redisDemoService.countBits(request.key()));
    }

    @PostMapping("/hyperloglog/add")
    public Map<String, Object> addHyperLogLog(@RequestBody HyperLogLogRequest request) {
        return mapOf("size", redisDemoService.addHyperLogLog(request.key(), request.values()));
    }

    @PostMapping("/hyperloglog/count")
    public Map<String, Object> countHyperLogLog(@RequestBody KeyRequest request) {
        return mapOf("size", redisDemoService.countHyperLogLog(request.key()));
    }

    @PostMapping("/geo/add")
    public Map<String, Object> addGeo(@RequestBody GeoRequest request) {
        return mapOf("size", redisDemoService.addGeoLocation(request.key(), request.member(), request.longitude(), request.latitude()));
    }

    @PostMapping("/geo/distance")
    public Map<String, Object> geoDistance(@RequestBody GeoDistanceRequest request) {
        return mapOf("distance", redisDemoService.distance(request.key(), request.member1(), request.member2()));
    }

    @PostMapping("/redisson/lock")
    public Map<String, Object> tryLock(@RequestBody LockRequest request) {
        boolean locked = redissonDemoService.tryLock(
                request.lockName(),
                Duration.ofSeconds(request.waitSeconds()),
                Duration.ofSeconds(request.leaseSeconds())
        );
        return mapOf("locked", locked);
    }

    @PostMapping("/redisson/counter")
    public Map<String, Object> increment(@RequestBody KeyRequest request) {
        return mapOf("value", redissonDemoService.increment(request.key()));
    }

    @PostMapping("/redisson/rate-limit")
    public Map<String, Object> rateLimit(@RequestBody RateLimitRequest request) {
        return mapOf("allowed", redissonDemoService.tryAcquire(request.rateLimiterName(), request.permitsPerSecond()));
    }

    private Map<String, Object> mapOf(String key1, Object value1, Object... others) {
        java.util.LinkedHashMap<String, Object> map = new java.util.LinkedHashMap<>();
        map.put(key1, value1);
        for (int i = 0; i < others.length; i += 2) {
            map.put((String) others[i], others[i + 1]);
        }
        return map;
    }
}
