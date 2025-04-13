package ru.yandex.practicum.intershop.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.intershop.dto.AddToCartRequestDto;
import ru.yandex.practicum.intershop.dto.ItemResponseDto;
import ru.yandex.practicum.intershop.service.ItemService;
import ru.yandex.practicum.intershop.service.OrderService;

@Controller
@RequiredArgsConstructor
@RequestMapping
@Slf4j
public class MainController {

    private final ItemService itemService;
    private final OrderService orderService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "NO") String sort,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpSession session,
            Model model
    ) {
        Page<ItemResponseDto> itemDtos = itemService.getBySearchPageable(search, sort, pageSize, session.getId());
        model.addAttribute("sort", sort);
        model.addAttribute("items", itemDtos);
        return "main";
    }

    @PostMapping(value = "/main/items/{itemId}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String addToCartMain(@PathVariable int itemId, AddToCartRequestDto request, HttpSession session) {
        orderService.addToOrder(itemId, request.getAction(), session.getId());
        return "redirect:/";
    }
}
