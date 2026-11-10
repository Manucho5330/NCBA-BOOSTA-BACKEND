package com.ncba.boosta.auth.services;

import com.ncba.boosta.auth.models.AuthAccount;
import com.ncba.boosta.auth.repository.AuthAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;

@Service
public class AuthService {
    private static final Set<String> ROLES =
            Set.of("ADMIN", "BANK_MAKER", "BANK_CHECKER", "CUSTOMER", "ANCHOR");
    private static final long TOKEN_LIFETIME_SECONDS = 3600;

    private final AuthAccountRepository accounts;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final String bootstrapSecret;
    private final String serviceSecret;

    public AuthService(
            AuthAccountRepository accounts,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${boosta.auth.bootstrap-secret:}") String bootstrapSecret,
            @Value("${boosta.auth.service-secret:}") String serviceSecret) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.bootstrapSecret = bootstrapSecret;
        this.serviceSecret = serviceSecret;
    }

    public LoginResult login(String identifier, String password) {
        if (password == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
        String normalizedIdentifier = normalize(identifier);
        AuthAccount account = accounts.findByIdentifier(normalizedIdentifier)
                .filter(AuthAccount::isEnabled)
                .filter(candidate -> passwordEncoder.matches(password, candidate.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials."));

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(TOKEN_LIFETIME_SECONDS);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("ncba-boosta")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(account.getIdentifier())
                .claim("role", account.getRole())
                .claim("subjectId", account.getSubjectId() == null ? "" : account.getSubjectId())
                .build();
        return new LoginResult(jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue(),
                account.getRole(), account.getSubjectId(), expiresAt);
    }

    public void bootstrapAdmin(String suppliedSecret, String identifier, String password) {
        requireSecret(bootstrapSecret, suppliedSecret, "Admin bootstrap is not configured.");
        if (accounts.existsByAdminMarker("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An administrator already exists.");
        }
        validatePassword(password, false);
        saveAccount(normalizeEmail(identifier), "ADMIN", password, "ADMIN");
    }

    public void provision(String suppliedSecret, String identifier, String password, String role, String subjectId) {
        requireSecret(serviceSecret, suppliedSecret, "Service account provisioning is not configured.");
        String normalizedRole = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
        if (!ROLES.contains(normalizedRole) || normalizedRole.equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported managed account role.");
        }
        boolean customer = normalizedRole.equals("CUSTOMER");
        validatePassword(password, customer);
        String normalizedIdentifier = customer ? normalizePhone(identifier) : normalizeEmail(identifier);
        saveAccount(normalizedIdentifier, normalizedRole, password, subjectId);
    }

    public void setEnabled(String suppliedSecret, String identifier, boolean enabled) {
        requireSecret(serviceSecret, suppliedSecret, "Service account provisioning is not configured.");
        AuthAccount account = accounts.findByIdentifier(normalize(identifier))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found."));
        if (account.getRole().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator access cannot be disabled here.");
        }
        account.setEnabled(enabled);
        accounts.save(account);
    }

    private void saveAccount(String identifier, String role, String password, String subjectId) {
        if (accounts.existsByIdentifier(identifier)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account already uses this identifier.");
        }
        accounts.save(new AuthAccount(identifier, role, passwordEncoder.encode(password), subjectId));
    }

    private static void requireSecret(String expected, String supplied, String missingMessage) {
        if (expected == null || expected.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, missingMessage);
        }
        if (supplied == null || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid service credential.");
        }
    }

    private static void validatePassword(String password, boolean pin) {
        if (pin) {
            if (password == null || !password.matches("\\d{4}")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer PINs must contain four digits.");
            }
        } else if (password == null || password.length() < 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Passwords must contain at least 12 characters.");
        }
    }

    private static String normalize(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An identifier is required.");
        }
        String value = identifier.trim();
        return value.contains("@") ? normalizeEmail(value) : normalizePhone(value);
    }

    private static String normalizeEmail(String email) {
        if (email == null || !email.contains("@") || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid email address is required.");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizePhone(String phone) {
        if (phone == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A phone number is required.");
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() < 9 || digits.length() > 15) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid phone number is required.");
        }
        return digits;
    }

    public record LoginResult(String token, String role, String subjectId, Instant expiresAt) { }
}
