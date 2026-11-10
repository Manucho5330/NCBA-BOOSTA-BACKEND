package com.ncba.boosta.customer.dto.response;

import com.ncba.boosta.customer.models.CustomerNotification;

public record NotificationResponse(String id, String type, String title, String body, String date,
                                   boolean read, String state, String loanId, String actionUrl, String requestId) {
    public static NotificationResponse from(CustomerNotification notification) {
        return new NotificationResponse(notification.getId(), notification.getType(), notification.getTitle(),
                notification.getBody(), notification.getCreatedAt(), notification.isRead(),
                notification.getState().toLowerCase(),
                notification.getLoanId(), notification.getActionUrl(), notification.getRequestId());
    }
}
