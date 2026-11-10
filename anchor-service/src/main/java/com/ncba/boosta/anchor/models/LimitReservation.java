package com.ncba.boosta.anchor.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "anchor_limit_reservations")
public class LimitReservation {
    @Id
    private String id;
    private String anchorId;
    private long amount;
    private String status;

    protected LimitReservation() {
    }

    public LimitReservation(String id, String anchorId, long amount, String status) {
        this.id = id;
        this.anchorId = anchorId;
        this.amount = amount;
        this.status = status;
    }

    public String getId() { return id; }
    public String getAnchorId() { return anchorId; }
    public long getAmount() { return amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
