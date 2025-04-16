package ru.yandex.practicum.intershop.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class ItemResponseDto {
    private final Long id;
    private final String title;
    private final BigDecimal price;
    private final String description;
    private final String imgPath;
    private Integer count = 0;
}
