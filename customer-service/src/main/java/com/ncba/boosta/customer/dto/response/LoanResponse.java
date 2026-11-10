package com.ncba.boosta.customer.dto.response;

import com.ncba.boosta.customer.models.Loan;

public record LoanResponse(String id, String reference, String applicationId, String customerId,
                           String anchorId, long principal, long outstandingBalance,
                           String disbursedAt, String dueDate, String status) {
    public static LoanResponse from(Loan loan) {
        return new LoanResponse(loan.getId(), loan.getReference(), loan.getApplicationId(),
                loan.getCustomerId(), loan.getAnchorId(), loan.getPrincipal(),
                loan.getOutstandingBalance(), loan.getDisbursedAt(), loan.getDueDate(), loan.getStatus());
    }
}
