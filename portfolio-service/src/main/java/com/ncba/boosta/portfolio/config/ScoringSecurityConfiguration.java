package com.ncba.boosta.portfolio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

@Configuration
public class ScoringSecurityConfiguration {
    @Bean
    @Profile("!local-no-auth")
    JwtDecoder scoringJwtDecoder(@Value("${boosta.auth.jwt-secret:}") String secret) {
        if (secret.length() < 32) {
            throw new IllegalStateException("BOOSTA_AUTH_JWT_SECRET must contain at least 32 characters.");
        }
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    @Profile("!local-no-auth")
    SecurityFilterChain scoringSecurity(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/v1/scoring/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/v1/scoring/customers").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/scoring/anchors").hasRole("ANCHOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/scoring/**")
                        .hasAnyRole("CUSTOMER", "ANCHOR")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(scoringJwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    @Profile("local-no-auth")
    SecurityFilterChain localNoAuthScoringSecurity(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        return http.build();
    }

    private Converter<Jwt, org.springframework.security.authentication.AbstractAuthenticationToken>
    scoringJwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString("role");
            Collection<GrantedAuthority> authorities = role == null
                    ? List.of()
                    : List.of(new SimpleGrantedAuthority("ROLE_" + role));
            return authorities;
        });
        return converter;
    }
}
