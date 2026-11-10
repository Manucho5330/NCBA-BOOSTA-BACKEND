package com.ncba.boosta.customer.dto.request;

import com.ncba.boosta.customer.models.Loan;

public record LoanRequest(String id, String reference, String applicationId, String customerId,
                          String anchorId, long principal, long outstandingBalance,
                          String disbursedAt, String dueDate, String status) {
    public Loan toModel() {
        return new Loan(id, reference, applicationId, customerId, anchorId, principal,
                outstandingBalance, disbursedAt, dueDate, status);
    }
}
