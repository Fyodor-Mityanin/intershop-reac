package ru.yandex.practicum.intershop.shop.entity;



import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import ru.yandex.practicum.intershop.shop.dto.OrderStatus;

import java.time.LocalDateTime;

@Table("orders")
@Getter
@Setter
public class Order {
    @Id
    private Long id;
    @Column("order_time")
    private LocalDateTime orderTime;
    @Column("customer")
    private String customer;
    @Column("session")
    private String session;
    @Column("status")
    private OrderStatus status;
}