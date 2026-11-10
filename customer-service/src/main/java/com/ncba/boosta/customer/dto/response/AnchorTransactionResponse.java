package com.ncba.boosta.customer.dto.response;

public record AnchorTransactionResponse(String id, String reference, String date, String type,
                                        long amount, String status, String customerName) {
}
