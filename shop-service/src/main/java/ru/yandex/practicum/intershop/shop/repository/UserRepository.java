package ru.yandex.practicum.intershop.shop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.shop.entity.UserEntity;

@SuppressWarnings("unused")
@Repository
public interface UserRepository extends R2dbcRepository<UserEntity, Long> {

    Mono<UserEntity> findByLogin(String login);
}
