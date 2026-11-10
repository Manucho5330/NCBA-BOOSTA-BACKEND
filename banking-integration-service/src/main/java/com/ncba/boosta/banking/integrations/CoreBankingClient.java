package com.ncba.boosta.banking.integrations;

public interface CoreBankingClient {
    TransactionResult initiate(TransactionRequest request);
    TransactionResult getConfirmation(String transactionReference);

    record TransactionRequest(String customerId, String debitAccount, String creditAccount,
                              long amount, String currency, String idempotencyKey, String narration) { }
    record TransactionResult(String transactionReference, String status, String message,
                             String confirmedAt) { }
}
