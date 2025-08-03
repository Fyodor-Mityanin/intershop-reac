package ru.yandex.practicum.intershop.shop.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
@Slf4j
public class SecurityConfig {


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .anonymous(Customizer.withDefaults())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/login", "/logout", "/", "/items/**", "/images/**", "/debug/**").permitAll()
                        .anyExchange().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(
                                (exchange, authentication) -> exchange
                                        .getExchange()
                                        .getSession()
                                        .flatMap(WebSession::invalidate)
                                        .then(
                                                Mono.fromRunnable(
                                                        () -> {
                                                            ServerHttpResponse response = exchange
                                                                    .getExchange()
                                                                    .getResponse();
                                                            response.getCookies().clear();
                                                            response.setStatusCode(HttpStatus.FOUND);
                                                            response.getHeaders().setLocation(URI.create("/"));
                                                        }
                                                )
                                        )
                        )
                )
                .build();
    }
}