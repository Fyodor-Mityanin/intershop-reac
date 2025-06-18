package ru.yandex.practicum.intershop.shop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.shop.dto.AddToCartRequestDto;
import ru.yandex.practicum.intershop.shop.service.OrderService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart/items")
@Slf4j
public class CartController {

    private final OrderService orderService;

    @GetMapping
    public Mono<Rendering> getCart(WebSession session) {
        return Mono.just(
                Rendering.view("cart")
                        .modelAttribute("items", orderService.getNewBySession(session.getId()))
                        .modelAttribute("total", orderService.getTotalSumBySession(session.getId()))
                        .build()
        );
    }

    @PostMapping(value = "/{itemId}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<String> addToCart(@PathVariable Long itemId, AddToCartRequestDto request, WebSession session) {
        return orderService.addToOrder(itemId, request.getAction(), session.getId())
                .thenReturn("redirect:/cart/items");
    }

    @PostMapping(value = "/main/items/{itemId}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<String> addToCartMain(@PathVariable Long itemId, AddToCartRequestDto request, WebSession session) {
        return orderService.addToOrder(itemId, request.getAction(), session.getId())
                .thenReturn("redirect:/");
    }

    @PostMapping("/buy")
    public Mono<Rendering> buy(WebSession session) {
        return orderService.setStatusAndGet(session.getId())
                .map(orderId ->
                        Rendering.view("redirect:/orders/" + orderId)
                                .modelAttribute("newOrder", true)
                                .build()
                );
    }
}
