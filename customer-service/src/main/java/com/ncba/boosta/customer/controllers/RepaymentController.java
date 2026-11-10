package com.ncba.boosta.customer.controllers;

import com.ncba.boosta.customer.models.Loan;
import com.ncba.boosta.customer.models.PortfolioTransaction;
import com.ncba.boosta.customer.models.RepaymentRequest;
import com.ncba.boosta.customer.repository.LoanRepository;
import com.ncba.boosta.customer.repository.PortfolioTransactionRepository;
import com.ncba.boosta.customer.repository.RepaymentRequestRepository;
import com.ncba.boosta.customer.services.RepaymentService;
import com.ncba.boosta.customer.dto.request.RepaymentAmountRequest;
import com.ncba.boosta.customer.dto.request.RepaymentRequestBody;
import com.ncba.boosta.customer.dto.response.RepaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class RepaymentController {
    private final RepaymentService repaymentService;
    private final LoanRepository loans;
    private final RepaymentRequestRepository repayments;
    private final PortfolioTransactionRepository transactions;

    public RepaymentController(RepaymentService repaymentService, LoanRepository loans,
                               RepaymentRequestRepository repayments,
                               PortfolioTransactionRepository transactions) {
        this.repaymentService = repaymentService;
        this.loans = loans;
        this.repayments = repayments;
        this.transactions = transactions;
    }

    @PostMapping("/api/v1/repayments")
    public RepaymentResponse submit(@RequestBody RepaymentRequestBody body,
                                    @RequestHeader("Idempotency-Key") String idempotencyKey,
                                    @AuthenticationPrincipal Jwt jwt) {
        return submitForLoan(body.loanId(), body.amount(), body.method(), idempotencyKey, jwt);
    }

    @PatchMapping("/api/v1/loans/{loanId}/repayments")
    public RepaymentResponse submitForLoan(@PathVariable String loanId,
                                           @RequestBody RepaymentAmountRequest body,
                                           @RequestHeader("Idempotency-Key") String idempotencyKey,
                                           @AuthenticationPrincipal Jwt jwt) {
        return submitForLoan(loanId, body.amount(), "NCBA_ACCOUNT", idempotencyKey, jwt);
    }

    @GetMapping("/api/v1/repayments/{id}")
    public RepaymentResponse get(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        RepaymentRequest request = repayments.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Repayment not found."));
        authorizeCustomer(request.getCustomerId(), jwt);
        Loan loan = loans.findById(request.getLoanId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found."));
        PortfolioTransaction transaction = request.getStatus().equals("COMPLETED")
                ? transactions.findById("txn-" + request.getId()).orElse(null)
                : null;
        return RepaymentResponse.from(request, loan, transaction);
    }

    private RepaymentResponse submitForLoan(String loanId, long amount, String method,
                                            String idempotencyKey, Jwt jwt) {
        if (loanId == null || loanId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loan id is required.");
        }
        Loan loan = loans.findById(loanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found."));
        authorizeCustomer(loan.getCustomerId(), jwt);
        RepaymentRequest request = repaymentService.submit(loanId, amount, method, idempotencyKey);
        return RepaymentResponse.from(request, loan, null);
    }

    private static void authorizeCustomer(String customerId, Jwt jwt) {
        if ("CUSTOMER".equals(jwt.getClaimAsString("role"))
                && !customerId.equals(jwt.getClaimAsString("subjectId"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Customers can only access repayments for their own loans.");
        }
    }

}
