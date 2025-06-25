package ru.yandex.practicum.intershop.shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.shop.config.RedisConfig;
import ru.yandex.practicum.intershop.shop.dto.ItemResponseDto;
import ru.yandex.practicum.intershop.shop.dto.OrderStatus;
import ru.yandex.practicum.intershop.shop.entity.Order;
import ru.yandex.practicum.intershop.shop.repository.ItemRepository;
import ru.yandex.practicum.intershop.shop.repository.OrderItemRepository;
import ru.yandex.practicum.intershop.shop.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderCacheService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;

    @Cacheable(
            cacheNames = RedisConfig.CACHE_SESSION,
            key = "#sessionId"
    )
    public Mono<List<ItemResponseDto>> getNewBySession(String sessionId) {
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
                .sort(Comparator.comparing(ItemResponseDto::getId))
                .collectList();
    }
}
