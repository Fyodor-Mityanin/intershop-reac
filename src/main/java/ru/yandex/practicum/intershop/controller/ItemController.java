package ru.yandex.practicum.intershop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import ru.yandex.practicum.intershop.dto.AddToCartRequestDto;
import ru.yandex.practicum.intershop.dto.ItemResponseDto;
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
    public String addToCart(@PathVariable int itemId, AddToCartRequestDto request, WebSession session) {
        orderService.addToOrder(itemId, request.getAction(), session.getId());
        return "redirect:/items/{itemId}";
    }

    @GetMapping("/{itemId}")
    public String getItem(@PathVariable int itemId, Model model, WebSession session) {
        ItemResponseDto itemResponseDto = itemService.getById(itemId, session.getId());
        model.addAttribute("item", itemResponseDto);
        return "item";
    }
}
