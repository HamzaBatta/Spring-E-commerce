package com.codewithmosh.store.services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductViewTracker {

    private static final String VIEW_KEY_PREFIX = "product:views:";
    private static final String TOP_PRODUCTS_KEY = "product:top";
    private static final int TOP_N = 10;

    private final RedisTemplate<String, Object> redisTemplate;

    public ProductViewTracker(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void trackView(Long productId) {
        String key = VIEW_KEY_PREFIX + productId;
        redisTemplate.opsForValue().increment(key);
        redisTemplate.opsForZSet().incrementScore(
                TOP_PRODUCTS_KEY,
                productId.toString(),
                1
        );
    }

    public List<Long> getTopProductIds() {
        Set<Object> topIds = redisTemplate.opsForZSet().reverseRange(
                TOP_PRODUCTS_KEY, 0, TOP_N - 1
        );
        if (topIds == null) return Collections.emptyList();
        return topIds.stream()
                .map(id -> Long.parseLong(id.toString()))
                .collect(Collectors.toList());
    }

    public Long getViewCount(Long productId) {
        String key = VIEW_KEY_PREFIX + productId;
        Object count = redisTemplate.opsForValue().get(key);
        return count != null ? Long.parseLong(count.toString()) : 0L;
    }
}

