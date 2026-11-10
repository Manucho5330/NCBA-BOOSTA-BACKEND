package com.ncba.boosta.auth.controllers;

import com.ncba.boosta.auth.services.AuthService;
import com.ncba.boosta.auth.dto.request.BootstrapRequest;
import com.ncba.boosta.auth.dto.request.LoginRequest;
import com.ncba.boosta.auth.dto.request.ProvisionRequest;
import com.ncba.boosta.auth.dto.response.BootstrapResponse;
import com.ncba.boosta.auth.dto.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/v1/auth/login")
    @Operation(security = {})
    public LoginResponse login(@RequestBody LoginRequest request) {
        AuthService.LoginResult result = authService.login(request.identifier(), request.secret());
        return new LoginResponse(result.token(), result.role(), result.subjectId(), result.expiresAt());
    }

    @PostMapping("/api/v1/auth/bootstrap")
    @Operation(security = {})
    public BootstrapResponse bootstrap(
            @RequestHeader(value = "X-BOOSTA-BOOTSTRAP-SECRET", required = false) String secret,
            @RequestBody BootstrapRequest request) {
        authService.bootstrapAdmin(secret, request.identifier(), request.secret());
        return new BootstrapResponse("Administrator account created.");
    }

    @PostMapping("/internal/auth/accounts")
    public void provision(
            @RequestHeader(value = "X-BOOSTA-SERVICE-SECRET", required = false) String secret,
            @RequestBody ProvisionRequest request) {
        authService.provision(secret, request.identifier(), request.secret(), request.role(), request.subjectId());
    }

    @PatchMapping("/internal/auth/accounts/{identifier}/enabled")
    public void setEnabled(
            @RequestHeader(value = "X-BOOSTA-SERVICE-SECRET", required = false) String secret,
            @PathVariable String identifier,
            @RequestParam boolean enabled) {
        authService.setEnabled(secret, identifier, enabled);
    }
}
