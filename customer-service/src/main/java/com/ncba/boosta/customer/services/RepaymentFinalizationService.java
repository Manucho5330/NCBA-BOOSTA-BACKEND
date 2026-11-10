package com.ncba.boosta.customer.services;

import com.ncba.boosta.customer.models.Loan;
import com.ncba.boosta.customer.models.PortfolioTransaction;
import com.ncba.boosta.customer.models.RepaymentRequest;
import com.ncba.boosta.customer.repository.LoanRepository;
import com.ncba.boosta.customer.repository.PortfolioTransactionRepository;
import com.ncba.boosta.customer.repository.RepaymentRequestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Instant;

@Service
public class RepaymentFinalizationService {
    private final LoanRepository loans;
    private final RepaymentRequestRepository repayments;
    private final PortfolioTransactionRepository transactions;

    public RepaymentFinalizationService(LoanRepository loans, RepaymentRequestRepository repayments,
                                        PortfolioTransactionRepository transactions) {
        this.loans = loans;
        this.repayments = repayments;
        this.transactions = transactions;
    }

    @Transactional
    public void complete(String repaymentId, String bankingReference) {
        RepaymentRequest request = repayments.findById(repaymentId)
                .orElseThrow(() -> new IllegalStateException("Repayment request disappeared during confirmation."));
        if ("COMPLETED".equals(request.getStatus())) return;
        if (!"AWAITING_CONFIRMATION".equals(request.getStatus())) {
            throw new IllegalStateException("Only a pending repayment can be finalized.");
        }
        Loan loan = loans.findLockedById(request.getLoanId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found."));
        if (request.getAmount() > loan.getOutstandingBalance()) {
            throw new IllegalStateException("Confirmed repayment exceeds the loan's remaining balance.");
        }
        long balance = loan.getOutstandingBalance() - request.getAmount();
        loan.setOutstandingBalance(balance);
        if (balance == 0) loan.setStatus("PAID");
        loans.save(loan);

        String transactionId = "txn-" + request.getId();
        if (!transactions.existsById(transactionId)) {
            transactions.save(new PortfolioTransaction(transactionId, request.getReference(), bankingReference,
                    LocalDate.now().toString(), request.getAmount(),
                    balance == 0 ? "REPAYMENT" : "PARTIAL_REPAYMENT",
                    request.getAnchorId(), loan.getId(), "COMPLETED"));
        }
        request.setStatus("COMPLETED");
        request.setUpdatedAt(Instant.now().toString());
        repayments.save(request);
    }
}
