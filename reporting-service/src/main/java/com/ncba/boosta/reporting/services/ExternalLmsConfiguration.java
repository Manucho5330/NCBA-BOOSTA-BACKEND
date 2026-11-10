package com.ncba.boosta.reporting.services;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Configuration
public class ExternalLmsConfiguration {
    @Bean
    @ConditionalOnMissingBean(ExternalLmsPortfolioProvider.class)
    ExternalLmsPortfolioProvider unconfiguredExternalLmsPortfolioProvider() {
        return () -> {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "External LMS integration is not configured.");
        };
    }
}
