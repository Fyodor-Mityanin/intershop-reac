package ru.yandex.practicum.intershop.shop.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.shop.dto.OrderStatus;
import ru.yandex.practicum.intershop.shop.entity.Order;

import java.math.BigDecimal;

@Repository
public interface OrderRepository extends R2dbcRepository<Order, Long> {

    Flux<Order> findByUserLoginAndStatusNot(String userLogin, OrderStatus status);

    Mono<Order> findByUserLoginAndStatus(String userLogin, OrderStatus status);

    @Query("""
            SELECT
                SUM(oi.quantity * i.price) AS total_amount
            FROM orders o
                JOIN order_items oi ON o.id = oi.order_id
                JOIN items i ON oi.item_id = i.id
            WHERE o.user_login = :userLogin
            GROUP BY o.id;""")
    Mono<BigDecimal> getTotalSumByUserLogin(@Param("userLogin") String userLogin);
}
