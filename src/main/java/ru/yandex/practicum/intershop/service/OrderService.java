package ru.yandex.practicum.intershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public void addToOrder(int itemId, String action, String sessionId) {
        Item item = itemRepository.getReferenceById(itemId);
        Order order = getOrCreateBySession(sessionId);
        OrderItem orderItem = order.getOrderItems().stream()
                .filter(i -> i.getItem().getId().equals(item.getId()))
                .findFirst()
                .orElse(new OrderItem(item, order, 0));
        switch (action) {
            case "plus" -> {
                orderItem.setQuantity(orderItem.getQuantity() + 1);
                order.getOrderItems().add(orderItem);
            }
            case "minus" -> {
                int count = orderItem.getQuantity() == 0 ? 0 : orderItem.getQuantity() - 1;
                if (count == 0) {
                    order.getOrderItems().remove(orderItem);
                    orderItemRepository.delete(orderItem);
                } else {
                    orderItem.setQuantity(count);
                    order.getOrderItems().add(orderItem);
                }
            }
            case "delete" -> {
                order.getOrderItems().remove(orderItem);
                orderItemRepository.delete(orderItem);
            }
        }
        orderRepository.save(order);
    }

    public Order getOrCreateBySession(String session) {
        Optional<Order> order = orderRepository.findBySessionAndStatus(session, OrderStatus.NEW.name());
        if (order.isPresent()) {
            return order.get();
        }
        Order newOrder = new Order();
        newOrder.setSession(session);
        newOrder.setCustomer(ANONYMOUS_CUSTOMER);
        newOrder.setStatus(OrderStatus.NEW.name());
        return orderRepository.save(newOrder);
    }

    public Map<Integer, Integer> findOrderItemsMapBySession(String session) {
        return orderRepository.findBySessionAndStatus(session, OrderStatus.NEW.name())
                .map(orderMapper::toDto)
                .map(OrderDto::getOrderItems)
                .map(this::getOrderItemMap)
                .orElse(null);
    }

    private Map<Integer, Integer> getOrderItemMap(List<OrderItemDto> orderItemDto) {
        return orderItemDto.stream()
                .collect(
                        Collectors.toMap(
                                OrderItemDto::getItemId,
                                OrderItemDto::getQuantity
                        )
                );
    }

    @Transactional
    public List<ItemResponseDto> getNewBySession(String sessionId) {
        return orderRepository
                .findBySessionAndStatus(sessionId, OrderStatus.NEW.name())
                .map(Order::getOrderItems)
                .map(orderItemMapper::toDtos)
                .orElse(Collections.emptyList())
                .stream()
                .sorted(Comparator.comparing(ItemResponseDto::getId))
                .toList();
    }

    public Integer setStatusAndGet(String sessionId) {
        Order order = orderRepository.findBySessionAndStatus(sessionId, OrderStatus.NEW.name()).orElseThrow();
        order.setStatus(OrderStatus.PROCESSING.name());
        orderRepository.save(order);
        return order.getId();
    }

    @Transactional
    public OrderResponseDto getById(int orderId) {
        return orderMapper.toResponseDto(orderRepository.getReferenceById(orderId));
    }

    public List<OrderResponseDto> getBySession(String session) {
        return orderRepository.findBySessionAndStatusNot(session, OrderStatus.NEW.name()).stream()
                .map(orderMapper::toResponseDto)
                .collect(Collectors.toList());
    }
}
