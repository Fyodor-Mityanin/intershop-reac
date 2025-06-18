package ru.yandex.practicum.intershop.shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.shop.dto.ItemResponseDto;
import ru.yandex.practicum.intershop.shop.entity.Item;
import ru.yandex.practicum.intershop.shop.mapper.ItemMapper;
import ru.yandex.practicum.intershop.shop.repository.ItemRepository;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final OrderService orderService;

    public Flux<ItemResponseDto> getBySearchPageable(String search, String sortRaw, Integer pageSize, String session) {
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
                                itemRepository.findById(itemId)
                                        .map(itemMapper::toDto)
                                        .map(item -> {
                                            item.setCount(orderDto.getOrDefault(item.getId(), 0));
                                            return item;
                                        })
                );
    }
}

