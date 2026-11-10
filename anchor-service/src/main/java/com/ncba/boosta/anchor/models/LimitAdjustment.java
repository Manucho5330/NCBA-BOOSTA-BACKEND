package com.ncba.boosta.anchor.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "anchor_limit_adjustments")
public class LimitAdjustment {
    @Id
    private String id;
    private String anchorId;
    private long amount;

    protected LimitAdjustment() {
    }

    public LimitAdjustment(String id, String anchorId, long amount) {
        this.id = id;
        this.anchorId = anchorId;
        this.amount = amount;
    }

    public String getId() { return id; }
    public String getAnchorId() { return anchorId; }
    public long getAmount() { return amount; }
}
