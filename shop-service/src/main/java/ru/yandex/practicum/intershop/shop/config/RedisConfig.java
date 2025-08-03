package ru.yandex.practicum.intershop.shop.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.*;

@Configuration
@EnableCaching
public class RedisConfig {
    public static final String CACHE_ITEMS_ALL = "items:all";
    public static final String CACHE_ITEMS_ID = "items:id";
    public static final String CACHE_USER = "user";

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}
