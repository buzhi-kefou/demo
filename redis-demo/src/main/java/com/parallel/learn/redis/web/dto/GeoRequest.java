package com.parallel.learn.redis.web.dto;

public record GeoRequest(String key, String member, double longitude, double latitude) {
}
