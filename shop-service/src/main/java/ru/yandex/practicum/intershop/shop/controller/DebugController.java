package ru.yandex.practicum.intershop.shop.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @GetMapping("/auth")
    public Mono<Map<String, Object>> authInfo() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> {
                    Authentication auth = ctx.getAuthentication();
                    Map<String, Object> info = new HashMap<>();
                    info.put("class", auth.getClass().getName());
                    info.put("principal", auth.getPrincipal());
                    info.put("authorities", auth.getAuthorities());
                    info.put("authenticated", auth.isAuthenticated());
                    return info;
                })
                .defaultIfEmpty(Map.of("auth", "no security context"));
    }
}