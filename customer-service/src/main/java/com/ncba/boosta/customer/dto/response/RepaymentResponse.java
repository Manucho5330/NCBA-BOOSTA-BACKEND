package com.ncba.boosta.customer.dto.response;

import com.ncba.boosta.customer.models.Loan;
import com.ncba.boosta.customer.models.PortfolioTransaction;
import com.ncba.boosta.customer.models.RepaymentRequest;

public record RepaymentResponse(boolean success, String status, String ftxReference, long newBalance,
                                LoanResponse loan, TransactionResponse transaction,
                                String repaymentId, String declineReason) {
    public static RepaymentResponse from(RepaymentRequest request, Loan loan, PortfolioTransaction transaction) {
        boolean complete = "COMPLETED".equals(request.getStatus());
        return new RepaymentResponse(complete, request.getStatus(),
                complete ? request.getBankingReference() : null, loan.getOutstandingBalance(),
                LoanResponse.from(loan),
                transaction == null ? null : TransactionResponse.from(transaction),
                request.getId(), request.getFailureReason());
    }
}
