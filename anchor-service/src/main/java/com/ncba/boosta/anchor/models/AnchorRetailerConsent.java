package com.ncba.boosta.anchor.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AnchorRetailerConsent {
    @Column(name = "retailer_id")
    private String retailerId;
    @Column(name = "consent_reference")
    private String consentReference;
    @Column(name = "consent_recorded_at")
    private String recordedAt;

    protected AnchorRetailerConsent() {
    }

    public AnchorRetailerConsent(String retailerId, String consentReference, String recordedAt) {
        this.retailerId = retailerId;
        this.consentReference = consentReference;
        this.recordedAt = recordedAt;
    }

    public String getRetailerId() { return retailerId; }
    public String getConsentReference() { return consentReference; }
    public String getRecordedAt() { return recordedAt; }
}
