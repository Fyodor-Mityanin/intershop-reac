package ru.yandex.practicum.intershop.shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.payment.client.api.PaymentApi;
import ru.yandex.practicum.intershop.payment.client.model.BalanceResponse;
import ru.yandex.practicum.intershop.payment.client.model.ChargeRequest;
import ru.yandex.practicum.intershop.payment.client.model.PaymentResult;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentServiceClient {

    private final PaymentApi paymentApi;


    public Mono<Boolean> isBuyAvailable(Mono<BigDecimal> totalSum) {
        return paymentApi.getBalance()
                .map(BalanceResponse::getBalance)
                .zipWith(totalSum)
                .map(tuple -> {
                    BigDecimal balance = tuple.getT1();
                    BigDecimal sum = tuple.getT2();
                    return balance.compareTo(sum) >= 0;
                });
    }

    public Mono<Boolean> charge(Mono<BigDecimal> totalSum) {
        return totalSum
                .map(sum -> new ChargeRequest().amount(sum))
                .flatMap(chargeRequest -> paymentApi.chargePayment(chargeRequest)
                        .mapNotNull(PaymentResult::getSuccess)
                );
    }
}
