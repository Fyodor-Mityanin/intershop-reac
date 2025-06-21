package ru.yandex.practicum.intershop.payment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.payment.api.PaymentApi;
import ru.yandex.practicum.intershop.payment.api.model.BalanceResponse;
import ru.yandex.practicum.intershop.payment.api.model.ChargeRequest;
import ru.yandex.practicum.intershop.payment.api.model.PaymentResult;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentControllerImpl implements PaymentApi {

    @Value("${payment.initial-balance}")
    private double initialBalance;

    @Override
    public Mono<ResponseEntity<PaymentResult>> chargePayment(Mono<ChargeRequest> chargeRequest, ServerWebExchange exchange) {
        return chargeRequest
                .doOnNext(request -> log.info("Received chargePayment request: {}", request))
                .flatMap(request -> {
                            double newBalance = initialBalance - request.getAmount();
                            if (newBalance >= 0) {
                                log.info("Payment successful. Amount: {}, New balance: {}", request.getAmount(), newBalance);
                                initialBalance = newBalance;
                                return Mono.just(
                                        ResponseEntity.ok(
                                                new PaymentResult()
                                                        .success(true)
                                                        .newBalance(newBalance)
                                        )
                                );
                            } else {
                                log.warn("Payment failed due to insufficient balance. Requested amount: {}, Available balance: {}", request.getAmount(), initialBalance);
                                return Mono.just(
                                        ResponseEntity
                                                .badRequest()
                                                .body(
                                                        new PaymentResult()
                                                                .success(false)
                                                                .newBalance(initialBalance)
                                                )
                                );
                            }
                        }
                );
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(ServerWebExchange exchange) {
        log.info("Received getBalance request");
        return Mono.just(
                ResponseEntity
                        .ok(
                                new BalanceResponse()
                                        .balance(initialBalance)
                        )
        );
    }
}
