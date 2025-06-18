package ru.yandex.practicum.intershop.shop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.shop.dto.AddToCartRequestDto;
import ru.yandex.practicum.intershop.shop.service.ItemService;
import ru.yandex.practicum.intershop.shop.service.OrderService;

@Controller
@RequiredArgsConstructor
@RequestMapping
@Slf4j
public class MainController {

    private final ItemService itemService;
    private final OrderService orderService;

    @GetMapping
    public Mono<Rendering> index(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "NO") String sort,
            @RequestParam(defaultValue = "10") Integer pageSize,
            WebSession session
    ) {
        return Mono.just(
                Rendering.view("main")
                        .modelAttribute("sort", sort)
                        .modelAttribute("pageSize", pageSize)
                        .modelAttribute("items", itemService.getBySearchPageable(search, sort, pageSize, session.getId()))
                        .build()
        );
    }

    @PostMapping(value = "/main/items/{itemId}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<String> addToCartMain(@PathVariable Long itemId, AddToCartRequestDto request, WebSession session) {
        return orderService.addToOrder(itemId, request.getAction(), session.getId())
                .thenReturn("redirect:/");
    }
}
