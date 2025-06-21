package ru.yandex.practicum.intershop.shop.config;

import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.yandex.practicum.intershop.shop.dto.ItemResponseDto;
import ru.yandex.practicum.intershop.shop.entity.Item;

import java.util.List;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, Item> reactiveRedisItemTemplate(ReactiveRedisConnectionFactory factory) {
        RedisSerializationContext<String, Item> context = RedisSerializationContext
                .<String, Item>newSerializationContext(new StringRedisSerializer())
                .value(new Jackson2JsonRedisSerializer<>(Item.class))
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, List<ItemResponseDto>> reactiveRedisListItemResponseDtoTemplate(
            ReactiveRedisConnectionFactory factory) {

        Jackson2JsonRedisSerializer<List<ItemResponseDto>> serializer = new Jackson2JsonRedisSerializer<>(
                TypeFactory
                        .defaultInstance()
                        .constructCollectionType(List.class, ItemResponseDto.class)
        );

        RedisSerializationContext<String, List<ItemResponseDto>> context = RedisSerializationContext
                .<String, List<ItemResponseDto>>newSerializationContext(RedisSerializer.string())
                .value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
