package ru.yandex.practicum.intershop.shop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.shop.dto.AddToCartRequestDto;
import ru.yandex.practicum.intershop.shop.service.OrderService;
import ru.yandex.practicum.intershop.shop.service.PaymentServiceClient;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart/items")
@Slf4j
public class CartController {

    private final OrderService orderService;
    private final PaymentServiceClient paymentServiceClient;

    @GetMapping
    public Mono<Rendering> getCart(WebSession session) {
        String sessionId = session.getId();
        Mono<BigDecimal> totalSum = orderService.getTotalSumBySession(sessionId)
                .doOnNext(sum -> System.out.println("💰 totalSum = " + sum));
        Mono<Boolean> isBuyAvailable = paymentServiceClient.isBuyAvailable(totalSum)
                .doOnNext(available -> System.out.println("🟢 isBuyAvailable = " + available));
        return Mono
                .zip(
                        orderService.getNewBySession(sessionId).collectList()
                                .doOnNext(items -> {
                                    System.out.println("🛒 items:");
                                    items.forEach(item -> System.out.println(" - " + item));
                                }),
                        totalSum,
                        isBuyAvailable
                )
                .map(tuple -> Rendering.view("cart")
                        .modelAttribute("items", tuple.getT1())
                        .modelAttribute("total", tuple.getT2())
                        .modelAttribute("isBuyAvailable", tuple.getT3())
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
        String sessionId = session.getId();
        return orderService.getTotalSumBySession(sessionId)
                .flatMap(totalSum -> paymentServiceClient.charge(Mono.just(totalSum)))
                .flatMap(success -> success
                        ? orderService.setStatusAndGet(sessionId)
                        .map(orderId -> Rendering.view("redirect:/orders/" + orderId)
                                .modelAttribute("newOrder", true)
                                .build())
                        : Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough balance"))
                );
    }
}
