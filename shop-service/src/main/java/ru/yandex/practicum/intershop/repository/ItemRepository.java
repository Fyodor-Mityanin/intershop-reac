package ru.yandex.practicum.intershop.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.intershop.entity.Item;

@Repository
public interface ItemRepository extends R2dbcRepository<Item, Long> {

    Flux<Item> findByTitleContainsIgnoreCase(String title, Pageable pageable);

    @Query("SELECT * FROM items LIMIT :limit OFFSET :offset")
    Flux<Item> findAllPageable(long limit, long offset);
}
