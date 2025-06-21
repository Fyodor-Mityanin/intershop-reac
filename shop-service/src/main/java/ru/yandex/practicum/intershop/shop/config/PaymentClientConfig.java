package ru.yandex.practicum.intershop.shop.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.intershop.payment.client.ApiClient;
import ru.yandex.practicum.intershop.payment.client.api.PaymentApi;

@Configuration
@Slf4j
public class PaymentClientConfig {

    @Value("${payment.api.url}")
    private String paymentApiUrl;

    @Bean
    public ApiClient paymentApiClient() {
        log.info("paymentApiUrl: {}", paymentApiUrl);
        ApiClient client = new ApiClient();
        client.setBasePath(paymentApiUrl);
        return client;
    }

    @Bean
    public PaymentApi paymentApi(ApiClient paymentApiClient) {
        return new PaymentApi(paymentApiClient);
    }
}