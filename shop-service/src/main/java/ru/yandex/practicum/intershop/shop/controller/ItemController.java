package ru.yandex.practicum.intershop.shop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.shop.dto.AddToCartRequestDto;
import ru.yandex.practicum.intershop.shop.service.ItemService;
import ru.yandex.practicum.intershop.shop.service.OrderService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/items")
@Slf4j
public class ItemController {

    private final ItemService itemService;
    private final OrderService orderService;

    @PostMapping(value = "/{itemId}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<String> addToCart(@PathVariable Long itemId, AddToCartRequestDto request, Authentication authentication) {
        return orderService.addToOrder(itemId, request.getAction(), authentication.getName())
                .thenReturn("redirect:/items/{itemId}");
    }

    @GetMapping("/{itemId}")
    public Mono<Rendering> getItem(@PathVariable Long itemId, Authentication authentication) {
        return Mono.just(
                Rendering.view("item")
                        .modelAttribute("item", itemService.getById(itemId, authentication.getName()))
                        .build()
        );
    }
}
