package com.ncba.boosta.anchor.config;

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
public class AnchorSecurityConfiguration {
    @Bean
    @Profile("!local-no-auth")
    JwtDecoder anchorJwtDecoder(@Value("${boosta.auth.jwt-secret:}") String secret) {
        if (secret.length() < 32) {
            throw new IllegalStateException("BOOSTA_AUTH_JWT_SECRET must contain at least 32 characters.");
        }
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    @Profile("!local-no-auth")
    SecurityFilterChain anchorSecurity(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/anchor-portal/**").hasRole("ANCHOR")
                        .requestMatchers("/api/v1/anchor-onboardings/**").hasAnyRole(
                                "ADMIN", "BANK_MAKER", "BANK_CHECKER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/anchors/*/disbursement-details")
                        .hasRole("BANK_CHECKER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/anchors").hasAnyRole(
                                "ADMIN", "BANK_MAKER", "BANK_CHECKER", "CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/anchors/**").hasAnyRole(
                                "ADMIN", "BANK_MAKER", "BANK_CHECKER", "ANCHOR", "CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/anchors/*/approved-limit").hasRole("BANK_CHECKER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/anchors/*/reservations/**").hasRole("BANK_CHECKER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/anchors/*/reservations").hasRole("BANK_CHECKER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/anchors/*/limit-adjustments").hasRole("BANK_CHECKER")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(anchorJwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    @Profile("local-no-auth")
    SecurityFilterChain localNoAuthAnchorSecurity(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        return http.build();
    }

    private JwtAuthenticationConverter anchorJwtAuthenticationConverter() {
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
