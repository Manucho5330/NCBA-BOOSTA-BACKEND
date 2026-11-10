package com.ncba.boosta.customer.dto.request;

public record CreateDrawdownRequest(String customerId, String anchorId, long amount, int tenorDays, String purpose) {
}
