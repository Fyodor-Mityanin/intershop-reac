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
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderCacheService orderCacheService;

    @CacheEvict(cacheNames = RedisConfig.CACHE_USER, key = "#userLogin")
    public Mono<Void> addToOrder(Long itemId, String action, String userLogin) {
        log.info("Start addToOrder: itemId={}, action={}, userLogin={}", itemId, action, userLogin);
        return Mono.zip(itemRepository.findById(itemId), getOrCreateByUserLogin(userLogin))
                .flatMap(tuple -> {
                    Item item = tuple.getT1();
                    Order order = tuple.getT2();
                    log.debug("Using order: {}", order);
                    return orderItemRepository.findByOrderIdAndItemId(order.getId(), item.getId())
                            .defaultIfEmpty(new OrderItem(item, order))
                            .flatMap(orderItem -> handleAction(action, itemId, orderItem));
                })
                .doOnTerminate(() -> log.info("Completed addToOrder for userLogin={}", userLogin))
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


    private Mono<Order> getOrCreateByUserLogin(String userLogin) {
        return orderRepository.findByUserLoginAndStatus(userLogin, OrderStatus.NEW)
                .switchIfEmpty(createNewOrder(userLogin));
    }

    private Mono<Order> createNewOrder(String userLogin) {
        Order newOrder = new Order();
        newOrder.setUserLogin(userLogin);
        newOrder.setStatus(OrderStatus.NEW);
        return orderRepository.save(newOrder);
    }

    public Mono<Map<Long, Integer>> findOrderItemsMapByLogin(String login) {
        return orderRepository.findByUserLoginAndStatus(login, OrderStatus.NEW)
                .map(Order::getId)
                .flatMapMany(orderItemRepository::findByOrderId)
                .collectMap(OrderItem::getItemId, OrderItem::getQuantity);
    }

    public Flux<ItemResponseDto> getNewByUserLogin(String userLogin) {
        return orderCacheService.getNewByUserLogin(userLogin).flatMapMany(Flux::fromIterable);
    }

    public Mono<Long> setStatusAndGet(String userLogin) {
        return orderRepository.findByUserLoginAndStatus(userLogin, OrderStatus.NEW)
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

    public Flux<OrderResponseDto> getByUserLogin(String userLogin) {
        return orderRepository.findByUserLoginAndStatusNot(userLogin, OrderStatus.NEW)
                .flatMap(this::getItems);
    }

    public Mono<BigDecimal> getTotalSumByUserLogin(String userLogin) {
        return orderRepository.getTotalSumByUserLogin(userLogin);
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
