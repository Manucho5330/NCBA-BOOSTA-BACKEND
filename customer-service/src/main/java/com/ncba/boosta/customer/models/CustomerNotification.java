package com.ncba.boosta.customer.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_notifications")
public class CustomerNotification {
    @Id
    private String id;
    @Column(nullable = false)
    private String ownerId;
    @Column(nullable = false)
    private String type;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, length = 2000)
    private String body;
    @Column(nullable = false)
    private String createdAt;
    @Column(nullable = false)
    private String state;
    private String loanId;
    private String actionUrl;
    private String requestId;

    public String getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getCreatedAt() { return createdAt; }
    public boolean isRead() { return "READ".equals(state) || "ARCHIVED".equals(state); }
    public String getState() { return state; }
    public String getLoanId() { return loanId; }
    public String getActionUrl() { return actionUrl; }
    public String getRequestId() { return requestId; }
    public void setId(String id) { this.id = id; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public void setType(String type) { this.type = type; }
    public void setTitle(String title) { this.title = title; }
    public void setBody(String body) { this.body = body; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setState(String state) { this.state = state; }
    public void setLoanId(String loanId) { this.loanId = loanId; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
}
