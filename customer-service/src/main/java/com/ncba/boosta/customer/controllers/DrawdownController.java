package com.ncba.boosta.customer.controllers;

import com.ncba.boosta.customer.models.Customer;
import com.ncba.boosta.customer.models.DrawdownRequest;
import com.ncba.boosta.customer.models.Loan;
import com.ncba.boosta.customer.dto.request.CreateDrawdownRequest;
import com.ncba.boosta.customer.dto.response.DrawdownResponse;
import com.ncba.boosta.customer.dto.response.DrawdownSummaryResponse;
import com.ncba.boosta.customer.dto.response.LoanResponse;
import com.ncba.boosta.customer.dto.response.TransactionResponse;
import com.ncba.boosta.customer.repository.CustomerRepository;
import com.ncba.boosta.customer.repository.DrawdownRequestRepository;
import com.ncba.boosta.customer.repository.LoanRepository;
import com.ncba.boosta.customer.repository.PortfolioTransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
public class DrawdownController {
    private final CustomerRepository customers;
    private final DrawdownRequestRepository drawdowns;
    private final LoanRepository loans;
    private final PortfolioTransactionRepository transactions;

    public DrawdownController(CustomerRepository customers, DrawdownRequestRepository drawdowns,
                              LoanRepository loans, PortfolioTransactionRepository transactions) {
        this.customers = customers;
        this.drawdowns = drawdowns;
        this.loans = loans;
        this.transactions = transactions;
    }

    @PostMapping("/api/v1/drawdowns")
    public DrawdownResponse create(@RequestBody CreateDrawdownRequest body,
                                 @RequestHeader("Idempotency-Key") String idempotencyKey,
                                 @AuthenticationPrincipal Jwt jwt) {
        if (body.customerId() == null || body.customerId().isBlank()
                || body.anchorId() == null || body.anchorId().isBlank()
                || body.amount() <= 0 || body.tenorDays() <= 0
                || body.purpose() == null || body.purpose().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid customer, anchor, amount, tenor and purpose are required.");
        }
        if ("CUSTOMER".equals(jwt.getClaimAsString("role"))
                && !body.customerId().equals(jwt.getClaimAsString("subjectId"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Customers can only request drawdowns for their own account.");
        }
        Customer customer = customers.findById(body.customerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer does not exist."));
        if (customer.getNcbaAccountNumber() == null || customer.getNcbaAccountNumber().isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The customer must have an NCBA account before requesting a drawdown.");
        }
        if (idempotencyKey.isBlank() || idempotencyKey.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid Idempotency-Key is required.");
        }
        DrawdownRequest existing = drawdowns.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existing != null) {
            if (!existing.getCustomerId().equals(body.customerId())
                    || !existing.getAnchorId().equals(body.anchorId())
                    || existing.getAmount() != body.amount()
                    || existing.getTenorDays() != body.tenorDays()
                    || !existing.getPurpose().equals(body.purpose())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Idempotency-Key was already used for a different drawdown request.");
            }
            return response(existing);
        }
        DrawdownRequest drawdown = new DrawdownRequest();
        drawdown.setId("dd-" + UUID.randomUUID().toString());
        drawdown.setIdempotencyKey(idempotencyKey);
        drawdown.setReference("BOO-DR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        drawdown.setCustomerId(body.customerId());
        drawdown.setAnchorId(body.anchorId());
        drawdown.setAmount(body.amount());
        drawdown.setTenorDays(body.tenorDays());
        drawdown.setPurpose(body.purpose());
        drawdown.setStatus("PENDING");
        drawdown.setCreatedAt(Instant.now().toString());
        drawdown.setUpdatedAt(drawdown.getCreatedAt());
        return response(drawdowns.save(drawdown));
    }

    @GetMapping("/api/v1/drawdowns/{id}")
    public DrawdownResponse get(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        DrawdownRequest drawdown = drawdowns.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Drawdown not found."));
        if ("CUSTOMER".equals(jwt.getClaimAsString("role"))
                && !drawdown.getCustomerId().equals(jwt.getClaimAsString("subjectId"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customers may only read their own drawdowns.");
        }
        return response(drawdown);
    }

    @GetMapping("/api/v1/drawdowns")
    public List<DrawdownResponse> list(@AuthenticationPrincipal Jwt jwt) {
        List<DrawdownRequest> customerDrawdowns = "CUSTOMER".equals(jwt.getClaimAsString("role"))
                ? drawdowns.findByCustomerIdOrderByCreatedAtDesc(jwt.getClaimAsString("subjectId"))
                : drawdowns.findAll();
        return customerDrawdowns.stream().map(this::response).toList();
    }

    @GetMapping("/api/v1/transactions")
    public List<TransactionResponse> transactions(@AuthenticationPrincipal Jwt jwt) {
        if ("CUSTOMER".equals(jwt.getClaimAsString("role"))) {
            List<String> loanIds = loans.findByCustomerId(jwt.getClaimAsString("subjectId")).stream()
                    .map(Loan::getId)
                    .toList();
            return transactions.findAll().stream()
                    .filter(transaction -> loanIds.contains(transaction.getLoanId()))
                    .map(TransactionResponse::from)
                    .toList();
        }
        return transactions.findAll().stream().map(TransactionResponse::from).toList();
    }

    private DrawdownResponse response(DrawdownRequest request) {
        Loan loan = request.getLoanId() == null ? null : loans.findById(request.getLoanId()).orElse(null);
        String status = "AWAITING_CONFIRMATION".equals(request.getStatus())
                ? "PENDING" : request.getStatus();
        DrawdownSummaryResponse summary = new DrawdownSummaryResponse(request.getId(), request.getReference(),
                request.getAnchorId(), request.getAmount(), request.getTenorDays(), request.getPurpose(),
                "DECLINED".equals(request.getStatus()) ? "REJECTED" : status,
                request.getCustomerId(), "SINGLE", List.of(), request.getCreatedAt(),
                "DISBURSED".equals(request.getStatus()) ? request.getUpdatedAt() : null, request.getLoanId());
        return new DrawdownResponse("DECLINED".equals(status) ? "DECLINED" : status, summary,
                loan == null ? null : LoanResponse.from(loan), request.getFailureReason());
    }
}
