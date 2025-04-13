package ru.yandex.practicum.intershop.mapper;

import org.mapstruct.*;
import ru.yandex.practicum.intershop.dto.ItemResponseDto;
import ru.yandex.practicum.intershop.entity.Item;
import ru.yandex.practicum.intershop.entity.OrderItem;

@Mapper
public interface ItemMapper {
    @Mapping(target = "count", ignore = true)
    ItemResponseDto toDto(Item source);

    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "title", source = "item.title")
    @Mapping(target = "price", source = "item.price")
    @Mapping(target = "description", source = "item.description")
    @Mapping(target = "imgPath", source = "item.imgPath")
    @Mapping(target = "count", source = "quantity")
    ItemResponseDto toDto(OrderItem source);
}
