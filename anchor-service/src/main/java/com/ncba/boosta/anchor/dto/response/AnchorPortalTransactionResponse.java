package com.ncba.boosta.anchor.dto.response;

public record AnchorPortalTransactionResponse(String id, String reference, String date, String type,
                                              long amount, String status, String customerName) {
}
