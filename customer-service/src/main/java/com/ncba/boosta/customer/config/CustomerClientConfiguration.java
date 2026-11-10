package com.ncba.boosta.customer.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class CustomerClientConfiguration {
    @Bean
    @Primary
    WebClient.Builder externalWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    @LoadBalanced
    @Qualifier("serviceWebClientBuilder")
    WebClient.Builder serviceWebClientBuilder() {
        return WebClient.builder();
    }
}
