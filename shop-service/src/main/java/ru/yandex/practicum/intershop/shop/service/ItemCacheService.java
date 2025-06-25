package ru.yandex.practicum.intershop.shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.shop.config.RedisConfig;
import ru.yandex.practicum.intershop.shop.entity.Item;
import ru.yandex.practicum.intershop.shop.repository.ItemRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemCacheService {

    private final ItemRepository itemRepository;

    @Cacheable(
            cacheNames = RedisConfig.CACHE_ITEMS_ALL,
            key = "#sortRaw + ':' + #pageSize",
            condition = "#search == null || #search.length() == 0"
    )
    public Flux<Item> getItemsSearchPageable(String search, String sortRaw, Integer pageSize) {
        log.info("getItemsSearchPageable load from DB: search={},sortRaw={},pageSize={}", search, sortRaw, pageSize);
        Sort sort = switch (sortRaw) {
            case "ALFA" -> Sort.by("title");
            case "PRICE" -> Sort.by("price");
            default -> Sort.unsorted();
        };
        Pageable pageable = PageRequest.of(0, pageSize, sort);
        Flux<Item> items;
        if (StringUtils.hasLength(search)) {
            items = itemRepository.findByTitleContainsIgnoreCase(search, pageable);
        } else {
            items = itemRepository.findAllPageable(pageSize, 0);
        }
        return items;
    }

    @Cacheable(
            cacheNames = RedisConfig.CACHE_ITEMS_ID,
            key = "#id"
    )
    public Mono<Item> findById(Long id) {
        return itemRepository.findById(id);
    }
}
