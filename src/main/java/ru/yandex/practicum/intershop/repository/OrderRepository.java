package ru.yandex.practicum.intershop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.dto.OrderStatus;
import ru.yandex.practicum.intershop.entity.Order;

@Repository
public interface OrderRepository extends R2dbcRepository<Order, Long> {

    Flux<Order> findBySessionAndStatusNot(String session, OrderStatus status);

    Mono<Order> findBySessionAndStatus(String session, OrderStatus status);
}
