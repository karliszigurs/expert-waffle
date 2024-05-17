package com.zigurs.mintos.ledger.infra.fxrates;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class ApiClientConfig {

    @Value("${currency_beakon_api_connection_timeout}")
    private int connectionTimeout;

    @Value("${currency_beakon_api_read_timeout}")
    private int requestTimeout;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(connectionTimeout))
                .setReadTimeout(Duration.ofSeconds(requestTimeout))
                .build();
    }

}
