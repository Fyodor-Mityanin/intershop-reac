package ru.yandex.practicum.intershop.shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.shop.entity.Item;
import ru.yandex.practicum.intershop.shop.repository.ItemRepository;

import java.time.Duration;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class ItemCacheService {
    private final ItemRepository itemRepository;
    private final ReactiveRedisTemplate<String, Item> redisTemplate;

    private static final String CACHE_KEY = "items:all";
    private static final Duration CACHE_TTL = Duration.ofMinutes(1); // TTL = 1 минута

    public Mono<Item> findById(Long id) {
        String key = buildKey(id);
        return redisTemplate.opsForValue()
                .get(key)
                .switchIfEmpty(
                        itemRepository.findById(id)
                                .flatMap(item -> redisTemplate
                                        .opsForValue()
                                        .set(key, item, CACHE_TTL)
                                        .thenReturn(item)
                                )
                );
    }

    private String buildKey(Long id) {
        return "item::" + id;
    }

    public Flux<Item> findAllFilteredPageable(String search, Pageable pageable) {
        return findAll()
                .filter(matchesSearch(search))
                .transform(applySortAndPaging(pageable));
    }

    public Flux<Item> findAllPageable(Pageable pageable) {
        return findAll()
                .transform(applySortAndPaging(pageable));
    }

    private Predicate<Item> matchesSearch(String search) {
        String lowered = search.toLowerCase();
        return item -> item.getTitle().toLowerCase().contains(lowered);
    }

    private Function<Flux<Item>, Flux<Item>> applySortAndPaging(Pageable pageable) {
        return flux -> flux
                .sort(itemComparator(pageable.getSort()))
                .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                .take(pageable.getPageSize());
    }

    private Comparator<Item> itemComparator(Sort sort) {
        return (a, b) -> {
            for (Sort.Order order : sort) {
                int cmp = switch (order.getProperty()) {
                    case "title" -> a.getTitle().compareToIgnoreCase(b.getTitle());
                    case "price" -> a.getPrice().compareTo(b.getPrice());
                    default -> 0;
                };
                if (cmp != 0) return order.isAscending() ? cmp : -cmp;
            }
            return 0;
        };
    }

    private Flux<Item> findAll() {
        return redisTemplate
                .opsForList()
                .size(CACHE_KEY)
                .flatMapMany(size -> {
                    if (size == 0) {
                        return itemRepository.findAll()
                                .collectList()
                                .flatMapMany(items -> {
                                    if (items.isEmpty()) {
                                        return Flux.empty();
                                    }
                                    return redisTemplate
                                            .opsForList()
                                            .rightPushAll(CACHE_KEY, items)
                                            .then(redisTemplate.expire(CACHE_KEY, CACHE_TTL))
                                            .thenMany(Flux.fromIterable(items));
                                });
                    } else {
                        return redisTemplate.opsForList().range(CACHE_KEY, 0, -1);
                    }
                });
    }
}
