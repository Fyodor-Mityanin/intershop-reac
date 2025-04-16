package ru.yandex.practicum.intershop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.WebSession;
import ru.yandex.practicum.intershop.dto.OrderResponseDto;
import ru.yandex.practicum.intershop.service.OrderService;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public String getOrders(Model model, WebSession session) {
        List<OrderResponseDto> orders = orderService.getBySession(session.getId());
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/{orderId}")
    public String getOrder(@PathVariable int orderId, Model model) {
        OrderResponseDto orderResponseDto = orderService.getById(orderId);
        model.addAttribute("order", orderResponseDto);
        return "order";
    }

}
