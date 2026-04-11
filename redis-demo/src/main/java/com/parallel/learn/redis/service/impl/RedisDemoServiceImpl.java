package com.parallel.learn.redis.service.impl;

import com.parallel.learn.redis.config.RedisDemoProperties;
import com.parallel.learn.redis.service.RedisDemoService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisDemoServiceImpl implements RedisDemoService {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisDemoProperties properties;

    public RedisDemoServiceImpl(StringRedisTemplate stringRedisTemplate,
                                RedisTemplate<String, Object> redisTemplate,
                                RedisDemoProperties properties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @Override
    public void setString(String key, String value, Duration ttl) {
        stringRedisTemplate.opsForValue().set(withPrefix(key), value, ttl);
    }

    @Override
    public String getString(String key) {
        return stringRedisTemplate.opsForValue().get(withPrefix(key));
    }

    @Override
    public void putHash(String key, Map<String, Object> values) {
        redisTemplate.opsForHash().putAll(withPrefix(key), values);
    }

    @Override
    public Map<Object, Object> getHash(String key) {
        return redisTemplate.opsForHash().entries(withPrefix(key));
    }

    @Override
    public Long addToList(String key, List<String> values) {
        return stringRedisTemplate.opsForList().rightPushAll(withPrefix(key), values);
    }

    @Override
    public List<String> getList(String key, long start, long end) {
        List<String> list = stringRedisTemplate.opsForList().range(withPrefix(key), start, end);
        return list == null ? List.of() : list;
    }

    @Override
    public Long addToSet(String key, List<String> values) {
        String[] members = values.toArray(String[]::new);
        return stringRedisTemplate.opsForSet().add(withPrefix(key), members);
    }

    @Override
    public Long countSetMembers(String key) {
        Long size = stringRedisTemplate.opsForSet().size(withPrefix(key));
        return size == null ? 0L : size;
    }

    @Override
    public Long addToZSet(String key, String value, double score) {
        Boolean added = stringRedisTemplate.opsForZSet().add(withPrefix(key), value, score);
        return Boolean.TRUE.equals(added) ? 1L : 0L;
    }

    @Override
    public List<String> getTopZSet(String key, long start, long end) {
        Set<String> values = stringRedisTemplate.opsForZSet().reverseRange(withPrefix(key), start, end);
        return values == null ? List.of() : new ArrayList<>(values);
    }

    @Override
    public Boolean setBit(String key, long offset, boolean value) {
        return stringRedisTemplate.opsForValue().setBit(withPrefix(key), offset, value);
    }

    @Override
    public Boolean getBit(String key, long offset) {
        return stringRedisTemplate.opsForValue().getBit(withPrefix(key), offset);
    }

    @Override
    public Long countBits(String key) {
        return stringRedisTemplate.execute((RedisCallback<Long>) connection ->
                connection.bitCount(withPrefix(key).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    @Override
    public Long addHyperLogLog(String key, List<String> values) {
        String[] members = values.toArray(String[]::new);
        return stringRedisTemplate.opsForHyperLogLog().add(withPrefix(key), members);
    }

    @Override
    public Long countHyperLogLog(String key) {
        Long size = stringRedisTemplate.opsForHyperLogLog().size(withPrefix(key));
        return size == null ? 0L : size;
    }

    @Override
    public Long addGeoLocation(String key, String member, double longitude, double latitude) {
        return stringRedisTemplate.opsForGeo().add(withPrefix(key), new Point(longitude, latitude), member);
    }

    @Override
    public Double distance(String key, String member1, String member2) {
        Distance distance = stringRedisTemplate.opsForGeo().distance(withPrefix(key), member1, member2);
        return distance == null ? null : distance.getValue();
    }

    private String withPrefix(String key) {
        return properties.getKeyPrefix() + key;
    }
}
