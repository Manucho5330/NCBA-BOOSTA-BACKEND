package com.ncba.boosta.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class AuthSecurityConfiguration {
    @Bean
    @Profile("!local-no-auth")
    SecurityFilterChain authSecurity(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/v3/api-docs", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/bootstrap").permitAll()
                        .requestMatchers(HttpMethod.POST, "/internal/auth/accounts").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/internal/auth/accounts/**").permitAll()
                        .anyRequest().denyAll());
        return http.build();
    }

    @Bean
    @Profile("local-no-auth")
    SecurityFilterChain localNoAuthAuthSecurity(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        return http.build();
    }
}
