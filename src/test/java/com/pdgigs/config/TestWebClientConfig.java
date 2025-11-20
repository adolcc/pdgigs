package com.pdgigs.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

@TestConfiguration
public class TestWebClientConfig {

    @Bean
    public WebTestClient webTestClient(ApplicationContext context) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(20 * 1024 * 1024))
                .build();

        return WebTestClient
                .bindToApplicationContext(context)
                .configureClient()
                .exchangeStrategies(strategies)
                .build();
    }
}