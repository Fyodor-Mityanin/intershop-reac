package ru.yandex.practicum.intershop.shop.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
@Getter
@Setter
public class UserEntity {

    @Id
    private Long id;

    private String login;
    private String password;
    private String role;
}

