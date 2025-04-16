package ru.yandex.practicum.intershop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.entity.OrderItem;

@Repository
public interface OrderItemRepository extends R2dbcRepository<OrderItem, Long> {

    Flux<OrderItem> findByOrderId(Long orderId);

    Mono<OrderItem> findByOrderIdAndItemId(Long orderId, Long itemId);
}
