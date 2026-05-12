package com.textrpg.common;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class MemoryCache {
    private final Map<String, CacheEntry> stringMap = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Double>> zsetMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public MemoryCache() {
        scheduler.scheduleAtFixedRate(this::cleanExpired, 10, 10, TimeUnit.SECONDS);
    }

    public void set(String key, String value, long ttlSeconds) {
        long expireAt = ttlSeconds > 0 ? System.currentTimeMillis() + ttlSeconds * 1000 : -1;
        stringMap.put(key, new CacheEntry(value, expireAt));
    }

    public String get(String key) {
        CacheEntry entry = stringMap.get(key);
        if (entry == null) return null;
        if (entry.expireAt > 0 && System.currentTimeMillis() > entry.expireAt) {
            stringMap.remove(key);
            return null;
        }
        return entry.value;
    }

    /** 获取缓存项剩余存活秒数，不存在或已过期返回 0 */
    public long getTtlRemaining(String key) {
        CacheEntry entry = stringMap.get(key);
        if (entry == null) return 0;
        if (entry.expireAt <= 0) return 0;
        long remain = (entry.expireAt - System.currentTimeMillis()) / 1000;
        return Math.max(0, remain);
    }

    public void delete(String key) {
        stringMap.remove(key);
    }

    public void zadd(String key, String member, double score) {
        zsetMap.computeIfAbsent(key, k -> new ConcurrentHashMap<>()).put(member, score);
    }

    public LinkedHashMap<String, Double> zrevrangeWithScores(String key, long start, long end) {
        Map<String, Double> map = zsetMap.get(key);
        if (map == null) return new LinkedHashMap<>();
        LinkedHashMap<String, Double> result = new LinkedHashMap<>();
        map.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .skip(start)
                .limit(end - start + 1)
                .forEach(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }

    private void cleanExpired() {
        long now = System.currentTimeMillis();
        stringMap.entrySet().removeIf(e -> e.getValue().expireAt > 0 && now > e.getValue().expireAt);
    }

    private static class CacheEntry {
        String value;
        long expireAt;
        CacheEntry(String value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }
    }
}
