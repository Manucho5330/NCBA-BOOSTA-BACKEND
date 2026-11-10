package com.ncba.boosta.customer.dto.request;

public record RepaymentRequestBody(String loanId, long amount, String method) {
}
