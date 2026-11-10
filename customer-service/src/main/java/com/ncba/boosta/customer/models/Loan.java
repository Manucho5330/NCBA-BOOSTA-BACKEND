package com.ncba.boosta.customer.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "loans")
public class Loan {
    @Id
    private String id;
    private String reference;
    private String applicationId;
    private String customerId;
    private String anchorId;
    private long principal;
    private long outstandingBalance;
    private String disbursedAt;
    private String dueDate;
    private String status;

    protected Loan() {
    }

    public Loan(String id, String reference, String applicationId, String customerId, String anchorId,
                long principal, long outstandingBalance, String disbursedAt, String dueDate, String status) {
        this.id = id;
        this.reference = reference;
        this.applicationId = applicationId;
        this.customerId = customerId;
        this.anchorId = anchorId;
        this.principal = principal;
        this.outstandingBalance = outstandingBalance;
        this.disbursedAt = disbursedAt;
        this.dueDate = dueDate;
        this.status = status;
    }

    public String getId() { return id; }
    public String getReference() { return reference; }
    public String getApplicationId() { return applicationId; }
    public String getCustomerId() { return customerId; }
    public String getAnchorId() { return anchorId; }
    public long getPrincipal() { return principal; }
    public long getOutstandingBalance() { return outstandingBalance; }
    public String getDisbursedAt() { return disbursedAt; }
    public String getDueDate() { return dueDate; }
    public String getStatus() { return status; }
    public void setId(String id) { this.id = id; }
    public void setReference(String reference) { this.reference = reference; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public void setAnchorId(String anchorId) { this.anchorId = anchorId; }
    public void setPrincipal(long principal) { this.principal = principal; }
    public void setOutstandingBalance(long outstandingBalance) { this.outstandingBalance = outstandingBalance; }
    public void setDisbursedAt(String disbursedAt) { this.disbursedAt = disbursedAt; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public void setStatus(String status) { this.status = status; }
}
