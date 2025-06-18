package ru.yandex.practicum.intershop.shop.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Accessors(chain = true)
@Getter
@Setter
public class ItemResponseDto {
    private Long id;
    private String title;
    private BigDecimal price;
    private String description;
    private String imgPath;
    private Integer count = 0;
}
