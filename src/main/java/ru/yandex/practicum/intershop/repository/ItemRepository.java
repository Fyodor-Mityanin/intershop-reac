package ru.yandex.practicum.intershop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.intershop.entity.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

    Page<Item> findByTitleContainsIgnoreCase(String title, Pageable pageable);
}
