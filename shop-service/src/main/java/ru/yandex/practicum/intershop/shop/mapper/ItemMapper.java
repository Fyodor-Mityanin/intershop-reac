package ru.yandex.practicum.intershop.shop.mapper;

import org.mapstruct.*;
import ru.yandex.practicum.intershop.shop.dto.ItemResponseDto;
import ru.yandex.practicum.intershop.shop.entity.Item;

@Mapper
public interface ItemMapper {
    @Mapping(target = "count", ignore = true)
    ItemResponseDto toDto(Item source);
}
