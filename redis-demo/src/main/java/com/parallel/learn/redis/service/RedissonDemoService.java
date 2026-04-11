package com.parallel.learn.redis.service;

import java.time.Duration;

public interface RedissonDemoService {

    boolean tryLock(String lockName, Duration waitTime, Duration leaseTime);

    long increment(String counterName);

    boolean tryAcquire(String rateLimiterName, long permitsPerSecond);
}
