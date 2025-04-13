package ru.yandex.practicum.intershop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.yandex.practicum.intershop.dto.OrderDto;
import ru.yandex.practicum.intershop.dto.OrderItemDto;
import ru.yandex.practicum.intershop.dto.OrderResponseDto;
import ru.yandex.practicum.intershop.entity.Order;
import ru.yandex.practicum.intershop.entity.OrderItem;

import java.math.BigDecimal;
import java.util.Set;

@Mapper(uses = {ItemMapper.class})
public interface OrderMapper {

    OrderDto toDto(Order source);

    @Mapping(target = "items", source = "orderItems")
    @Mapping(target = "totalSum", source = "orderItems", qualifiedByName = "getTotalSum")
    OrderResponseDto toResponseDto(Order source);

    @Mapping(target = "itemId", source = "item.id")
    OrderItemDto toDto(OrderItem source);

    @Named("getTotalSum")
    default BigDecimal getTotalSum(Set<OrderItem> source) {
        return source.stream()
                .map(i -> i.getItem().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
