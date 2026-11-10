package com.ncba.boosta.customer.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "loan_transactions")
public class PortfolioTransaction {
    @Id
    private String id;
    private String reference;
    private String ftxReference;
    private String date;
    private long amount;
    private String type;
    private String anchorId;
    private String loanId;
    private String status;

    protected PortfolioTransaction() {
    }

    public PortfolioTransaction(String id, String reference, String bankingReference, String date,
                                long amount, String type, String anchorId, String loanId, String status) {
        this.id = id;
        this.reference = reference;
        this.ftxReference = bankingReference;
        this.date = date;
        this.amount = amount;
        this.type = type;
        this.anchorId = anchorId;
        this.loanId = loanId;
        this.status = status;
    }

    public String getId() { return id; }
    public String getReference() { return reference; }
    public String getFtxReference() { return ftxReference; }
    public String getDate() { return date; }
    public long getAmount() { return amount; }
    public String getType() { return type; }
    public String getAnchorId() { return anchorId; }
    public String getLoanId() { return loanId; }
    public String getStatus() { return status; }
}
