package com.ncba.boosta.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfiguration {
    @Bean
    @Profile("!local-no-auth")
    ReactiveJwtDecoder jwtDecoder(@Value("${boosta.auth.jwt-secret:}") String secret) {
        if (secret.length() < 32) {
            throw new IllegalStateException("BOOSTA_AUTH_JWT_SECRET must contain at least 32 characters.");
        }
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    @Profile("!local-no-auth")
    SecurityWebFilterChain gatewaySecurity(
            ServerHttpSecurity http,
            Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/actuator/health").permitAll()
                        .pathMatchers(HttpMethod.GET, "/", "/favicon.ico", "/swagger-ui.html",
                                "/swagger-ui/**", "/webjars/**", "/v3/api-docs/**", "/openapi/**")
                        .permitAll()
                        .pathMatchers("/actuator/**").denyAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/bootstrap").permitAll()
                        .pathMatchers("/api/v1/auth/**").hasRole("ADMIN")
                        .pathMatchers("/api/v1/scoring/customers/**").hasRole("CUSTOMER")
                        .pathMatchers("/api/v1/scoring/anchors/**").hasRole("ANCHOR")
                        .pathMatchers("/api/v1/customer-data-warehouse/**")
                        .hasAnyRole("BANK_MAKER", "BANK_CHECKER", "ADMIN")
                        .pathMatchers("/api/v1/banking/**")
                        .hasAnyRole("CUSTOMER", "ANCHOR", "BANK_MAKER", "BANK_CHECKER")
                        .pathMatchers("/api/v1/audit/**", "/api/v1/reports/**")
                        .hasAnyRole("ADMIN", "BANK_MAKER", "BANK_CHECKER")
                        .pathMatchers("/api/v1/anchor-portal/**").hasRole("ANCHOR")
                        .pathMatchers(HttpMethod.GET, "/api/v1/anchors", "/api/v1/anchors/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/customers").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/customers").hasAnyRole("BANK_MAKER", "BANK_CHECKER", "ADMIN")
                        .pathMatchers("/api/v1/customers/**", "/api/v1/loans/**",
                                "/api/v1/portfolio/**", "/api/v1/transactions/**",
                                "/api/v1/repayments/**", "/api/v1/drawdowns/**").hasRole("CUSTOMER")
                        .pathMatchers(HttpMethod.POST, "/api/v1/anchors", "/api/v1/anchors/**")
                        .hasAnyRole("BANK_MAKER", "ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/anchors/**").hasAnyRole("BANK_MAKER", "ADMIN")
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .build();
    }

    @Bean
    @Profile("local-no-auth")
    SecurityWebFilterChain localNoAuthGatewaySecurity(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                .build();
    }

    @Bean
    Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString("role");
            Collection<GrantedAuthority> authorities = role == null
                    ? List.of()
                    : List.of(new SimpleGrantedAuthority("ROLE_" + role));
            return authorities;
        });
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}
