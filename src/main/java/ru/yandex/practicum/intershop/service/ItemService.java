package ru.yandex.practicum.intershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.intershop.dto.ItemResponseDto;
import ru.yandex.practicum.intershop.entity.Item;
import ru.yandex.practicum.intershop.mapper.ItemMapper;
import ru.yandex.practicum.intershop.repository.ItemRepository;

import java.util.Map;

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
            items = itemRepository.findAll(pageable);
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

    @Transactional
    public ItemResponseDto getById(int itemId, String session) {
        Item referenceById = itemRepository.getReferenceById(itemId);
        ItemResponseDto itemResponseDto = itemMapper.toDto(referenceById);
        Map<Integer, Integer> orderDto = orderService.findOrderItemsMapBySession(session);
        if (orderDto != null) {
            itemResponseDto.setCount(orderDto.get(itemResponseDto.getId()));
        }
        return itemResponseDto;
    }
}

