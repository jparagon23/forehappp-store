package com.forehapp.store.general.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                caffeineCache("public-products",    500,   180),
                caffeineCache("location-countries",  50, 86400),
                caffeineCache("location-states",    500, 86400),
                caffeineCache("location-cities",   5000, 86400)
        ));
        return manager;
    }

    private CaffeineCache caffeineCache(String name, int maxSize, long ttlSeconds) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
                .build());
    }
}
