package ru.yandex.practicum.intershop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.WebSession;
import ru.yandex.practicum.intershop.dto.AddToCartRequestDto;
import ru.yandex.practicum.intershop.dto.ItemResponseDto;
import ru.yandex.practicum.intershop.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart/items")
@Slf4j
public class CartController {

    private final OrderService orderService;

    @GetMapping
    public String getCart(Model model, WebSession session) {
        List<ItemResponseDto> items = orderService.getNewBySession(session.getId());
        BigDecimal total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("items", items);
        model.addAttribute("total", total);
        return "cart";
    }

    @PostMapping(value = "/{itemId}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String addToCart(@PathVariable int itemId, AddToCartRequestDto request, WebSession session) {
        orderService.addToOrder(itemId, request.getAction(), session.getId());
        return "redirect:/cart/items";
    }

    @PostMapping("/buy")
    public String buy(RedirectAttributes redirectAttrs, WebSession session) {
        Integer orderId = orderService.setStatusAndGet(session.getId());
        redirectAttrs.addFlashAttribute("newOrder", true);
        return "redirect:/orders/" + orderId;
    }
}
