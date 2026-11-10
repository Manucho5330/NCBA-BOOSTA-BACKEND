package com.ncba.boosta.customer.services;

import com.ncba.boosta.customer.models.DrawdownRequest;
import com.ncba.boosta.customer.models.Loan;
import com.ncba.boosta.customer.models.PortfolioTransaction;
import com.ncba.boosta.customer.repository.DrawdownRequestRepository;
import com.ncba.boosta.customer.repository.LoanRepository;
import com.ncba.boosta.customer.repository.PortfolioTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class DrawdownFinalizationService {
    private final DrawdownRequestRepository drawdowns;
    private final LoanRepository loans;
    private final PortfolioTransactionRepository transactions;

    public DrawdownFinalizationService(DrawdownRequestRepository drawdowns, LoanRepository loans,
                                       PortfolioTransactionRepository transactions) {
        this.drawdowns = drawdowns;
        this.loans = loans;
        this.transactions = transactions;
    }

    @Transactional
    public void disburse(String drawdownId, String bankingReference, String confirmedAt) {
        DrawdownRequest request = drawdowns.findById(drawdownId)
                .orElseThrow(() -> new IllegalStateException("Drawdown request disappeared during confirmation."));
        if ("DISBURSED".equals(request.getStatus())) return;
        if (!"AWAITING_CONFIRMATION".equals(request.getStatus())) {
            throw new IllegalStateException("Only a pending drawdown can be finalized.");
        }
        String loanId = "loan-" + request.getId();
        LocalDate disbursementDate = confirmedAt == null || confirmedAt.isBlank()
                ? LocalDate.now() : LocalDate.parse(confirmedAt.substring(0, 10));
        if (!loans.existsById(loanId)) {
            loans.save(new Loan(loanId,
                    "BOO-L-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    request.getId(), request.getCustomerId(), request.getAnchorId(), request.getAmount(),
                    request.getAmount(), disbursementDate.toString(),
                    disbursementDate.plusDays(request.getTenorDays()).toString(), "ACTIVE"));
        }
        String transactionId = "txn-" + request.getId();
        if (!transactions.existsById(transactionId)) {
            transactions.save(new PortfolioTransaction(transactionId, request.getReference(), bankingReference,
                    disbursementDate.toString(), request.getAmount(), "DISBURSEMENT",
                    request.getAnchorId(), loanId, "COMPLETED"));
        }
        request.setLoanId(loanId);
        request.setStatus("DISBURSED");
        request.setUpdatedAt(java.time.Instant.now().toString());
        drawdowns.save(request);
    }
}
