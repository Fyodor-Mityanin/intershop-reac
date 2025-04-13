package ru.yandex.practicum.intershop.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class OrderDto {
    private final Long id;
    private final LocalDateTime orderTime;
    private final String customer;
    private final String session;
    private final String status;
    private final List<OrderItemDto> orderItems;
}
