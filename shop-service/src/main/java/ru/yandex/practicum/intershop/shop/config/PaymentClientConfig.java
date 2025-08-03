package ru.yandex.practicum.intershop.shop.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.intershop.payment.client.ApiClient;
import ru.yandex.practicum.intershop.payment.client.api.PaymentApi;

@Configuration
@Slf4j
public class PaymentClientConfig {

    @Value("${payment.api.url}")
    private String paymentApiUrl;

    @Bean
    ReactiveOAuth2AuthorizedClientManager auth2AuthorizedClientManager(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ReactiveOAuth2AuthorizedClientService authorizedClientService
    ) {
        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);

        manager.setAuthorizedClientProvider(ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .refreshToken()
                .build()
        );
        return manager;
    }

    @Bean
    WebClient paymentWebClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        var oauth2Filter = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2Filter.setDefaultClientRegistrationId("keycloak");

        return WebClient.builder()
                .baseUrl(paymentApiUrl)
                .filter(oauth2Filter)
                .build();
    }

    @Bean
    public ApiClient paymentApiClient(WebClient webClient) {
        log.info("paymentApiUrl: {}", paymentApiUrl);
        ApiClient client = new ApiClient(webClient);
        client.setBasePath(paymentApiUrl);
        return client;
    }

    @Bean
    public PaymentApi paymentApi(ApiClient paymentApiClient) {
        return new PaymentApi(paymentApiClient);
    }
}