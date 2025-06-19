package ru.yandex.practicum.intershop.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.payment.api.PaymentApi;
import ru.yandex.practicum.intershop.payment.api.model.BalanceResponse;
import ru.yandex.practicum.intershop.payment.api.model.ChargeRequest;
import ru.yandex.practicum.intershop.payment.api.model.PaymentResult;

@RestController
@RequiredArgsConstructor
public class PaymentControllerImpl implements PaymentApi {

    @Value("${payment.initial-balance}")
    private double initialBalance;

    @Override
    public Mono<ResponseEntity<PaymentResult>> chargePayment(Mono<ChargeRequest> chargeRequest, ServerWebExchange exchange) {
        return chargeRequest.flatMap(request -> {
                    double newBalance = initialBalance - request.getAmount();
                    if (newBalance >= 0) {
                        return Mono.just(
                                ResponseEntity.ok(
                                        new PaymentResult()
                                                .success(true)
                                                .newBalance(newBalance)
                                )
                        );
                    } else {
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
        return Mono.just(
                ResponseEntity
                        .ok(
                                new BalanceResponse()
                                        .balance(initialBalance)
                        )
        );
    }
}
