package ru.yandex.practicum.intershop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.intershop.dto.ItemResponseDto;
import ru.yandex.practicum.intershop.entity.OrderItem;

import java.util.List;
import java.util.Set;

@Mapper
public interface OrderItemMapper {

    @Mapping(target = "id", source = "item.id" )
    @Mapping(target = "title", source = "item.title" )
    @Mapping(target = "price", source = "item.price" )
    @Mapping(target = "description", source = "item.description" )
    @Mapping(target = "imgPath", source = "item.imgPath" )
    @Mapping(target = "count", source = "quantity" )
    ItemResponseDto toDto(OrderItem source);

    List<ItemResponseDto> toDtos(Set<OrderItem> source);
}
