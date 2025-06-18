package ru.yandex.practicum.intershop.shop.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Data
@Accessors(chain = true)
public class OrderResponseDto {
    private final Long id;
    private final List<ItemResponseDto> items;
    private final BigDecimal totalSum;
}
