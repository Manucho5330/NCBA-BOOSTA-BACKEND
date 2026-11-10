package com.ncba.boosta.customer.dto.request;

public record CreateNotificationRequest(String ownerId, String type, String title, String body,
                                        String loanId, String actionUrl, String requestId) {
}
