package ru.yandex.practicum.intershop.entity;



import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("orders")
public class Order {

    @Id
    private Integer id;

    @Column("order_time")
    private LocalDateTime orderTime;

    @Column("customer")
    private String customer;

    @Column("session")
    private String session;

    //  NEW, PROCESSING, COMPLETED
    @Column("status")
    private String status;

//    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    private Set<OrderItem> orderItems = new LinkedHashSet<>();
}