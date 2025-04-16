package ru.yandex.practicum.intershop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.service.OrderService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Mono<Rendering> getOrders(WebSession session) {
        return Mono.just(
                Rendering.view("orders")
                        .modelAttribute("orders", orderService.getBySession(session.getId()))
                        .build()
        );
    }

    @GetMapping("/{orderId}")
    public Mono<Rendering> getOrder(@PathVariable Long orderId) {
        return Mono.just(
                Rendering.view("order")
                        .modelAttribute("order", orderService.getById(orderId))
                        .build()
        );
    }
}
