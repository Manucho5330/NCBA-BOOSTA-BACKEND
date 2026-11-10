package com.ncba.boosta.customer.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table(name = "repayment_requests")
public class RepaymentRequest {
    @Id
    private String id;
    @Column(unique = true, nullable = false)
    private String idempotencyKey;
    private String reference;
    private String loanId;
    private String customerId;
    private String anchorId;
    private long amount;
    private String method;
    private String status;
    private String bankingReference;
    private String failureReason;
    private String createdAt;
    private String updatedAt;

    public RepaymentRequest() {
    }

    public String getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getReference() { return reference; }
    public String getLoanId() { return loanId; }
    public String getCustomerId() { return customerId; }
    public String getAnchorId() { return anchorId; }
    public long getAmount() { return amount; }
    public String getMethod() { return method; }
    public String getStatus() { return status; }
    public String getBankingReference() { return bankingReference; }
    public String getFailureReason() { return failureReason; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setId(String id) { this.id = id; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public void setReference(String reference) { this.reference = reference; }
    public void setLoanId(String loanId) { this.loanId = loanId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public void setAnchorId(String anchorId) { this.anchorId = anchorId; }
    public void setAmount(long amount) { this.amount = amount; }
    public void setMethod(String method) { this.method = method; }
    public void setStatus(String status) { this.status = status; }
    public void setBankingReference(String bankingReference) { this.bankingReference = bankingReference; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
