package ru.yandex.practicum.intershop.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("items")
@Getter
@Setter
public class Item {

    @Id
    private Long id;
    @Column("title")
    private String title;
    @Column("price")
    private BigDecimal price;
    @Column("description")
    private String description;
    @Column("imgPath")
    private String imgPath;
}