package ru.yandex.practicum.intershop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.dto.AddToCartRequestDto;
import ru.yandex.practicum.intershop.service.ItemService;
import ru.yandex.practicum.intershop.service.OrderService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/items")
@Slf4j
public class ItemController {

    private final ItemService itemService;
    private final OrderService orderService;

    @PostMapping(value = "/{itemId}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<String> addToCart(@PathVariable Long itemId, AddToCartRequestDto request, WebSession session) {
        return orderService.addToOrder(itemId, request.getAction(), session.getId())
                .thenReturn("redirect:/items/{itemId}");
    }

    @GetMapping("/{itemId}")
    public Mono<Rendering> getItem(@PathVariable Long itemId, WebSession session) {
        return Mono.just(
                Rendering.view("item")
                        .modelAttribute("item", itemService.getById(itemId, session.getId()))
                        .build()
        );
    }
}
