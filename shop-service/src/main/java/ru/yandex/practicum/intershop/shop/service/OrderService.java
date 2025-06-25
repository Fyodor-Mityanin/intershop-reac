package ru.yandex.practicum.intershop.shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.shop.config.RedisConfig;
import ru.yandex.practicum.intershop.shop.dto.*;
import ru.yandex.practicum.intershop.shop.entity.Item;
import ru.yandex.practicum.intershop.shop.entity.Order;
import ru.yandex.practicum.intershop.shop.entity.OrderItem;
import ru.yandex.practicum.intershop.shop.repository.ItemRepository;
import ru.yandex.practicum.intershop.shop.repository.OrderItemRepository;
import ru.yandex.practicum.intershop.shop.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final static String ANONYMOUS_CUSTOMER = "anonymousCustomer";

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderCacheService orderCacheService;

    @CacheEvict(cacheNames = RedisConfig.CACHE_SESSION, key = "#sessionId")
    public Mono<Void> addToOrder(Long itemId, String action, String sessionId) {
        log.info("Start addToOrder: itemId={}, action={}, sessionId={}", itemId, action, sessionId);
        return Mono.zip(itemRepository.findById(itemId), getOrCreateBySession(sessionId))
                .flatMap(tuple -> {
                    Item item = tuple.getT1();
                    Order order = tuple.getT2();
                    log.debug("Using order: {}", order);
                    return orderItemRepository.findByOrderIdAndItemId(order.getId(), item.getId())
                            .defaultIfEmpty(new OrderItem(item, order))
                            .flatMap(orderItem -> handleAction(action, itemId, orderItem));
                })
                .doOnTerminate(() -> log.info("Completed addToOrder for sessionId={}", sessionId))
                .then();
    }

    private Mono<Void> handleAction(String action, Long itemId, OrderItem orderItem) {
        return switch (action) {
            case "plus" -> {
                int newQty = orderItem.getQuantity() + 1;
                orderItem.setQuantity(newQty);
                log.info("Increasing quantity for itemId={} to {}", itemId, newQty);
                yield orderItemRepository.save(orderItem)
                        .doOnSuccess(i -> log.debug("Saved order item: {}", i))
                        .then();
            }
            case "minus" -> {
                int updated = Math.max(orderItem.getQuantity() - 1, 0);
                if (updated == 0) {
                    log.info("Deleting item from order: itemId={}", itemId);
                    yield orderItemRepository.delete(orderItem)
                            .doOnSuccess(v -> log.debug("Deleted order item: {}", orderItem));
                } else {
                    orderItem.setQuantity(updated);
                    log.info("Decreasing quantity for itemId={} to {}", itemId, updated);
                    yield orderItemRepository.save(orderItem)
                            .doOnSuccess(i -> log.debug("Saved updated order item: {}", i))
                            .then();
                }
            }
            case "delete" -> {
                log.info("Explicit delete for itemId={} from order", itemId);
                yield orderItemRepository.delete(orderItem)
                        .doOnSuccess(v -> log.debug("Deleted order item: {}", orderItem));
            }
            default -> {
                log.warn("Unknown action: {}", action);
                yield Mono.error(new IllegalArgumentException("Unknown action: " + action));
            }
        };
    }


    private Mono<Order> getOrCreateBySession(String sessionId) {
        return orderRepository.findBySessionAndStatus(sessionId, OrderStatus.NEW)
                .switchIfEmpty(createNewOrder(sessionId));
    }

    private Mono<Order> createNewOrder(String sessionId) {
        Order newOrder = new Order();
        newOrder.setSession(sessionId);
        newOrder.setCustomer(ANONYMOUS_CUSTOMER);
        newOrder.setStatus(OrderStatus.NEW);
        return orderRepository.save(newOrder);
    }

    public Mono<Map<Long, Integer>> findOrderItemsMapBySession(String session) {
        return orderRepository.findBySessionAndStatus(session, OrderStatus.NEW)
                .map(Order::getId)
                .flatMapMany(orderItemRepository::findByOrderId)
                .collectMap(OrderItem::getItemId, OrderItem::getQuantity);
    }

    public Flux<ItemResponseDto> getNewBySession(String sessionId) {
        return orderCacheService.getNewBySession(sessionId).flatMapMany(Flux::fromIterable);
    }

    public Mono<Long> setStatusAndGet(String sessionId) {
        return orderRepository.findBySessionAndStatus(sessionId, OrderStatus.NEW)
                .switchIfEmpty(Mono.error(new IllegalStateException("Order not found")))
                .flatMap(order -> {
                    order.setStatus(OrderStatus.PROCESSING);
                    return orderRepository.save(order);
                })
                .map(Order::getId);
    }

    public Mono<OrderResponseDto> getById(Long orderId) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Order not found")))
                .flatMap(this::getItems);
    }

    public Flux<OrderResponseDto> getBySession(String session) {
        return orderRepository.findBySessionAndStatusNot(session, OrderStatus.NEW)
                .flatMap(this::getItems);
    }

    public Mono<BigDecimal> getTotalSumBySession(String sessionId) {
        return orderRepository.getTotalSumBySession(sessionId);
    }

    private Mono<OrderResponseDto> getItems(Order order) {
        return orderItemRepository.findByOrderId(order.getId())
                .flatMap(orderItem ->
                        itemRepository.findById(orderItem.getItemId())
                                .map(item -> new ItemResponseDto()
                                        .setId(item.getId())
                                        .setTitle(item.getTitle())
                                        .setDescription(item.getDescription())
                                        .setImgPath(item.getImgPath())
                                        .setCount(orderItem.getQuantity())
                                        .setPrice(item.getPrice())
                                )
                )
                .collectList()
                .map(items -> {
                    BigDecimal totalSum = items.stream()
                            .map(ItemResponseDto::getPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new OrderResponseDto(order.getId(), items, totalSum);
                });
    }
}
