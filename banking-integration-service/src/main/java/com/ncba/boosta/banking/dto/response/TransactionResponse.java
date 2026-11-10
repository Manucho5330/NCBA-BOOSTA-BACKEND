package com.ncba.boosta.banking.dto.response;

public record TransactionResponse(String transactionReference, String status, String message,
                                 String confirmedAt) {
}
