package com.parallel.learn.redis.web.dto;

public record LockRequest(String lockName, long waitSeconds, long leaseSeconds) {
}
