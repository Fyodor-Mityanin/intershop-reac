package ru.yandex.practicum.intershop.mapper;

import org.mapstruct.*;
import ru.yandex.practicum.intershop.dto.ItemResponseDto;
import ru.yandex.practicum.intershop.entity.Item;

@Mapper
public interface ItemMapper {
    @Mapping(target = "count", ignore = true)
    ItemResponseDto toDto(Item source);
}
