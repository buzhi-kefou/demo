package com.parallel.learn.redis.service.impl;

import com.parallel.learn.redis.service.RedissonDemoService;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RedissonClient;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.springframework.stereotype.Service;

@Service
public class RedissonDemoServiceImpl implements RedissonDemoService {

    private final RedissonClient redissonClient;

    public RedissonDemoServiceImpl(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean tryLock(String lockName, Duration waitTime, Duration leaseTime) {
        RLock lock = redissonClient.getLock(lockName);
        try {
            return lock.tryLock(waitTime.toMillis(), leaseTime.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public long increment(String counterName) {
        RAtomicLong counter = redissonClient.getAtomicLong(counterName);
        return counter.incrementAndGet();
    }

    @Override
    public boolean tryAcquire(String rateLimiterName, long permitsPerSecond) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimiterName);
        rateLimiter.trySetRate(RateType.OVERALL, permitsPerSecond, 1, RateIntervalUnit.SECONDS);
        return rateLimiter.tryAcquire();
    }
}
