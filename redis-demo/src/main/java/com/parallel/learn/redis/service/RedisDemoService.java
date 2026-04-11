package com.parallel.learn.redis.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public interface RedisDemoService {

    void setString(String key, String value, Duration ttl);

    String getString(String key);

    void putHash(String key, Map<String, Object> values);

    Map<Object, Object> getHash(String key);

    Long addToList(String key, List<String> values);

    List<String> getList(String key, long start, long end);

    Long addToSet(String key, List<String> values);

    Long countSetMembers(String key);

    Long addToZSet(String key, String value, double score);

    List<String> getTopZSet(String key, long start, long end);

    Boolean setBit(String key, long offset, boolean value);

    Boolean getBit(String key, long offset);

    Long countBits(String key);

    Long addHyperLogLog(String key, List<String> values);

    Long countHyperLogLog(String key);

    Long addGeoLocation(String key, String member, double longitude, double latitude);

    Double distance(String key, String member1, String member2);
}
