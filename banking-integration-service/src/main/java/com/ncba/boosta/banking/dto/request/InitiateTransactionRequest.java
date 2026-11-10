package com.ncba.boosta.banking.dto.request;

public record InitiateTransactionRequest(String customerId, String debitAccount, String creditAccount,
                                         long amount, String currency, String idempotencyKey, String narration) {
}
