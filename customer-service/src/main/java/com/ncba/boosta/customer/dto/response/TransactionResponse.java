package com.ncba.boosta.customer.dto.response;

import com.ncba.boosta.customer.models.PortfolioTransaction;

public record TransactionResponse(String id, String reference, String ftxReference, String date,
                                  long amount, String type, String anchorId, String loanId, String status) {
    public static TransactionResponse from(PortfolioTransaction transaction) {
        return new TransactionResponse(transaction.getId(), transaction.getReference(),
                transaction.getFtxReference(), transaction.getDate(), transaction.getAmount(),
                transaction.getType(), transaction.getAnchorId(), transaction.getLoanId(),
                transaction.getStatus());
    }
}
