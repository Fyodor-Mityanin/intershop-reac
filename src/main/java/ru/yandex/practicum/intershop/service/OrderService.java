package ru.yandex.practicum.intershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.dto.*;
import ru.yandex.practicum.intershop.entity.Item;
import ru.yandex.practicum.intershop.entity.Order;
import ru.yandex.practicum.intershop.entity.OrderItem;
import ru.yandex.practicum.intershop.mapper.OrderItemMapper;
import ru.yandex.practicum.intershop.mapper.OrderMapper;
import ru.yandex.practicum.intershop.repository.ItemRepository;
import ru.yandex.practicum.intershop.repository.OrderItemRepository;
import ru.yandex.practicum.intershop.repository.OrderRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final static String ANONYMOUS_CUSTOMER = "anonymousCustomer";

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final TransactionalOperator transactionalOperator;

    public Mono<Void> addToOrder(Long itemId, String action, String sessionId) {
        return transactionalOperator.execute(txStatus ->
                itemRepository.findById(itemId)
                        .zipWith(getOrCreateBySession(sessionId))
                        .flatMap(tuple -> {
                            Item item = tuple.getT1();
                            Order order = tuple.getT2();

                            return orderItemRepository.findByOrderIdAndItemId(order.getId(), item.getId())
                                    .defaultIfEmpty(new OrderItem(item, order))
                                    .flatMap(orderItem -> {
                                        switch (action) {
                                            case "plus" -> {
                                                orderItem.setQuantity(orderItem.getQuantity() + 1);
                                                return orderItemRepository.save(orderItem).then();
                                            }
                                            case "minus" -> {
                                                int updated = Math.max(orderItem.getQuantity() - 1, 0);
                                                if (updated == 0) {
                                                    return orderItemRepository.delete(orderItem);
                                                } else {
                                                    orderItem.setQuantity(updated);
                                                    return orderItemRepository.save(orderItem).then();
                                                }
                                            }
                                            case "delete" -> {
                                                return orderItemRepository.delete(orderItem);
                                            }
                                            default -> {
                                                return Mono.error(new IllegalArgumentException("Unknown action: " + action));
                                            }
                                        }
                                    });
                        })

        ).then();
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

    @Transactional
    public List<ItemResponseDto> getNewBySession(String sessionId) {
        return orderRepository
                .findBySessionAndStatus(sessionId, OrderStatus.NEW)
                .map(Order::getOrderItems)
                .map(orderItemMapper::toDtos)
                .orElse(Collections.emptyList())
                .stream()
                .sorted(Comparator.comparing(ItemResponseDto::getId))
                .toList();
    }

    public Integer setStatusAndGet(String sessionId) {
        Order order = orderRepository.findBySessionAndStatus(sessionId, OrderStatus.NEW).orElseThrow();
        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
        return order.getId();
    }

    @Transactional
    public OrderResponseDto getById(int orderId) {
        return orderMapper.toResponseDto(orderRepository.getReferenceById(orderId));
    }

    public List<OrderResponseDto> getBySession(String session) {
        return orderRepository.findBySessionAndStatusNot(session, OrderStatus.NEW).stream()
                .map(orderMapper::toResponseDto)
                .collect(Collectors.toList());
    }
}
