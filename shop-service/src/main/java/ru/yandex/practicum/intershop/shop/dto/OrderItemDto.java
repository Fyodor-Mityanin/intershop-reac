package ru.yandex.practicum.intershop.shop.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OrderItemDto {
    private final int itemId;
    private final int quantity;
}
