package ru.yandex.practicum.intershop.shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.shop.dto.ItemResponseDto;
import ru.yandex.practicum.intershop.shop.dto.OrderStatus;
import ru.yandex.practicum.intershop.shop.entity.Order;
import ru.yandex.practicum.intershop.shop.repository.ItemRepository;
import ru.yandex.practicum.intershop.shop.repository.OrderItemRepository;
import ru.yandex.practicum.intershop.shop.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderCacheService {

    public static final String CACHE_KEY = "session:new:";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final ReactiveRedisTemplate<String, List<ItemResponseDto>> redisTemplate;

    private static final Duration TTL = Duration.ofMinutes(1);

    public Flux<ItemResponseDto> getNewBySession(String sessionId) {
        String key = CACHE_KEY + sessionId;
        return redisTemplate.opsForValue()
                .get(key)
                .flatMapMany(Flux::fromIterable)
                .switchIfEmpty(
                        fetchFromDatabase(sessionId)
                                .collectList()
                                .flatMap(list -> redisTemplate.opsForValue()
                                        .set(key, list, TTL)
                                        .thenReturn(list))
                                .flatMapMany(Flux::fromIterable)
                );
    }

    private Flux<ItemResponseDto> fetchFromDatabase(String sessionId) {
        return orderRepository
                .findBySessionAndStatus(sessionId, OrderStatus.NEW)
                .map(Order::getId)
                .flatMapMany(orderItemRepository::findByOrderId)
                .flatMap(orderItem -> itemRepository.findById(orderItem.getItemId())
                        .map(item -> new ItemResponseDto()
                                .setId(orderItem.getItemId())
                                .setTitle(item.getTitle())
                                .setPrice(item.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                                .setDescription(item.getDescription())
                                .setImgPath(item.getImgPath())
                                .setCount(orderItem.getQuantity()))
                )
                .sort(Comparator.comparing(ItemResponseDto::getId));
    }

    public Mono<Boolean> evictNewBySession(String sessionId) {
        String key = CACHE_KEY + sessionId;
        return redisTemplate.delete(key)
                .map(count -> count > 0);
    }

}
