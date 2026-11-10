package com.ncba.boosta.banking.controllers;

import com.ncba.boosta.banking.integrations.CoreBankingClient;
import com.ncba.boosta.banking.dto.request.InitiateTransactionRequest;
import com.ncba.boosta.banking.dto.response.TransactionResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/banking/transactions")
public class BankingIntegrationController {
    private final CoreBankingClient coreBanking;

    public BankingIntegrationController(CoreBankingClient coreBanking) {
        this.coreBanking = coreBanking;
    }

    @PostMapping
    public TransactionResponse initiate(
            @RequestBody InitiateTransactionRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction details are required.");
        }
        if (jwt != null && "CUSTOMER".equals(jwt.getClaimAsString("role"))
                && !Objects.equals(request.customerId(), jwt.getClaimAsString("subjectId"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Customers can only initiate transactions for their own account.");
        }
        if (request.amount() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction amount must be positive.");
        }
        if (request.customerId() == null || request.customerId().isBlank()
                || request.debitAccount() == null || request.debitAccount().isBlank()
                || request.creditAccount() == null || request.creditAccount().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Customer and debit/credit account details are required.");
        }
        if (request.idempotencyKey() == null || request.idempotencyKey().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An idempotency key is required.");
        }
        CoreBankingClient.TransactionResult result = coreBanking.initiate(
                new CoreBankingClient.TransactionRequest(request.customerId(), request.debitAccount(),
                        request.creditAccount(), request.amount(), request.currency(),
                        request.idempotencyKey(), request.narration()));
        return new TransactionResponse(result.transactionReference(), result.status(),
                result.message(), result.confirmedAt());
    }

    @GetMapping("/{reference}/confirmation")
    public TransactionResponse confirmation(@PathVariable String reference) {
        CoreBankingClient.TransactionResult result = coreBanking.getConfirmation(reference);
        return new TransactionResponse(result.transactionReference(), result.status(),
                result.message(), result.confirmedAt());
    }
}
