package com.ncba.boosta.customer.services;

import com.ncba.boosta.customer.models.Loan;
import com.ncba.boosta.customer.models.RepaymentRequest;
import com.ncba.boosta.customer.repository.LoanRepository;
import com.ncba.boosta.customer.repository.RepaymentRequestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RepaymentService {
    private static final List<String> PENDING_STATUSES = List.of("PENDING", "AWAITING_CONFIRMATION");

    private final LoanRepository loans;
    private final RepaymentRequestRepository repayments;

    public RepaymentService(LoanRepository loans, RepaymentRequestRepository repayments) {
        this.loans = loans;
        this.repayments = repayments;
    }

    @Transactional
    public RepaymentRequest submit(String loanId, long amount, String method, String idempotencyKey) {
        if (amount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Repayment amount must be positive.");
        }
        if (!"NCBA_ACCOUNT".equals(method)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only NCBA_ACCOUNT repayments are currently supported by the core-banking adapter.");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid Idempotency-Key is required.");
        }
        RepaymentRequest existing = repayments.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existing != null) {
            if (!existing.getLoanId().equals(loanId) || existing.getAmount() != amount
                    || !existing.getMethod().equals(method)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Idempotency-Key was already used for a different repayment.");
            }
            return existing;
        }
        Loan loan = loans.findLockedById(loanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found."));
        if ("PAID".equals(loan.getStatus()) || loan.getOutstandingBalance() == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Loan is already paid.");
        }
        long alreadyPending = repayments.findByLoanIdAndStatusIn(loanId, PENDING_STATUSES).stream()
                .mapToLong(RepaymentRequest::getAmount).sum();
        if (amount > loan.getOutstandingBalance() - alreadyPending) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Repayment amount exceeds the uncommitted outstanding balance.");
        }

        String id = "rep-" + UUID.randomUUID();
        String now = Instant.now().toString();
        RepaymentRequest request = new RepaymentRequest();
        request.setId(id);
        request.setIdempotencyKey(idempotencyKey);
        request.setReference("BOO-RP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        request.setLoanId(loan.getId());
        request.setCustomerId(loan.getCustomerId());
        request.setAnchorId(loan.getAnchorId());
        request.setAmount(amount);
        request.setMethod(method);
        request.setStatus("PENDING");
        request.setCreatedAt(now);
        request.setUpdatedAt(now);
        return repayments.save(request);
    }
}
