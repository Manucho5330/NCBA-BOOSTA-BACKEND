package com.ncba.boosta.customer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class CustomerSecurityConfiguration {
    @Bean
    @Profile("!local-no-auth")
    JwtDecoder customerJwtDecoder(@Value("${boosta.auth.jwt-secret:}") String secret) {
        if (secret.length() < 32) {
            throw new IllegalStateException("BOOSTA_AUTH_JWT_SECRET must contain at least 32 characters.");
        }
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    @Profile("!local-no-auth")
    SecurityFilterChain customerSecurity(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/customers").hasAnyRole(
                                "ADMIN", "BANK_MAKER", "BANK_CHECKER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/customers/**").hasAnyRole(
                                "ADMIN", "BANK_MAKER", "BANK_CHECKER", "CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/customers").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/customers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/customer-data-warehouse/**").hasAnyRole(
                                "ADMIN", "BANK_MAKER", "BANK_CHECKER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/portfolio/summary").hasAnyRole(
                                "ADMIN", "BANK_MAKER", "BANK_CHECKER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/anchor-data/**").hasRole("ANCHOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/drawdowns").hasAnyRole(
                                "ADMIN", "BANK_MAKER", "CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/drawdowns", "/api/v1/drawdowns/**").hasAnyRole(
                                "ADMIN", "BANK_MAKER", "BANK_CHECKER", "CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/notifications", "/api/v1/notifications/**").hasAnyRole(
                                "ADMIN", "BANK_MAKER", "BANK_CHECKER", "CUSTOMER", "ANCHOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/notifications").hasAnyRole(
                                "ADMIN", "BANK_MAKER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/notifications/**").hasAnyRole(
                                "ADMIN", "BANK_MAKER", "BANK_CHECKER", "CUSTOMER", "ANCHOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/repayments").hasAnyRole(
                                "ADMIN", "BANK_MAKER", "CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/repayments/**").hasAnyRole(
                                "ADMIN", "BANK_MAKER", "BANK_CHECKER", "CUSTOMER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/loans/*/repayments").hasAnyRole(
                                "ADMIN", "BANK_MAKER", "CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/transactions").hasAnyRole(
                                "ADMIN", "BANK_MAKER", "BANK_CHECKER", "CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/loans").hasAnyRole(
                                "ADMIN", "BANK_MAKER", "BANK_CHECKER", "CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/loans/**").hasAnyRole(
                                "ADMIN", "BANK_MAKER", "BANK_CHECKER", "CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/loans").hasAnyRole(
                                "ADMIN", "BANK_MAKER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/loans/**").hasAnyRole(
                                "ADMIN", "BANK_CHECKER")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(customerJwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    @Profile("local-no-auth")
    SecurityFilterChain localNoAuthCustomerSecurity(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        return http.build();
    }

    private JwtAuthenticationConverter customerJwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString("role");
            List<GrantedAuthority> authorities = role == null
                    ? List.of() : List.of(new SimpleGrantedAuthority("ROLE_" + role));
            return authorities;
        });
        return converter;
    }
}
