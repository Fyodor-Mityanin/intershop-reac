package ru.yandex.practicum.intershop.shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.shop.dto.ItemResponseDto;
import ru.yandex.practicum.intershop.shop.entity.Item;
import ru.yandex.practicum.intershop.shop.mapper.ItemMapper;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemMapper itemMapper;
    private final OrderService orderService;
    private final ItemCacheService itemCacheService;

    public Flux<ItemResponseDto> getBySearchPageable(String search, String sortRaw, Integer pageSize, String session) {
        Flux<Item> items = itemCacheService.getItemsSearchPageable(search, sortRaw, pageSize);
        return orderService.findOrderItemsMapBySession(session)
                .flatMapMany(
                        orderDto ->
                                items
                                        .map(itemMapper::toDto)
                                        .map(item -> {
                                            item.setCount(orderDto.getOrDefault(item.getId(), 0));
                                            return item;
                                        })
                );
    }

    public Mono<ItemResponseDto> getById(Long itemId, String session) {
        return orderService.findOrderItemsMapBySession(session)
                .flatMap(
                        orderDto ->
                                itemCacheService.findById(itemId)
                                        .map(itemMapper::toDto)
                                        .map(item -> {
                                            item.setCount(orderDto.getOrDefault(item.getId(), 0));
                                            return item;
                                        })
                );
    }
}

