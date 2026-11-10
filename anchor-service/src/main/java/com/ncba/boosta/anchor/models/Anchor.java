package com.ncba.boosta.anchor.models;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "anchors")
public class Anchor {
    @Id
    private String id;
    private String name;
    private String logoInitial;
    private String type;
    private long approvedLimit;
    private long availableLimit;
    private String crbStatus;
    private boolean drawdownAllowed;
    private String scorecardVersion;
    private double facilityFeePct;
    private String collectionAccountNumber;
    private String disbursementAccountNumber;
    private String category;
    private String tier;
    private Long annualTurnoverKes;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    @ElementCollection
    private List<Integer> allowedTenors = new ArrayList<>();

    public Anchor() {
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLogoInitial() { return logoInitial; }
    public String getType() { return type; }
    public long getApprovedLimit() { return approvedLimit; }
    public long getAvailableLimit() { return availableLimit; }
    public String getCrbStatus() { return crbStatus; }
    public boolean isDrawdownAllowed() { return drawdownAllowed; }
    public String getScorecardVersion() { return scorecardVersion; }
    public double getFacilityFeePct() { return facilityFeePct; }
    public String getCollectionAccountNumber() { return collectionAccountNumber; }
    public String getDisbursementAccountNumber() { return disbursementAccountNumber; }
    public String getCategory() { return category; }
    public String getTier() { return tier; }
    public Long getAnnualTurnoverKes() { return annualTurnoverKes; }
    public String getContactPerson() { return contactPerson; }
    public String getContactPhone() { return contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public List<Integer> getAllowedTenors() { return List.copyOf(allowedTenors); }
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLogoInitial(String logoInitial) { this.logoInitial = logoInitial; }
    public void setType(String type) { this.type = type; }
    public void setApprovedLimit(long approvedLimit) { this.approvedLimit = approvedLimit; }
    public void setAvailableLimit(long availableLimit) { this.availableLimit = availableLimit; }
    public void setCrbStatus(String crbStatus) { this.crbStatus = crbStatus; }
    public void setDrawdownAllowed(boolean drawdownAllowed) { this.drawdownAllowed = drawdownAllowed; }
    public void setScorecardVersion(String scorecardVersion) { this.scorecardVersion = scorecardVersion; }
    public void setFacilityFeePct(double facilityFeePct) { this.facilityFeePct = facilityFeePct; }
    public void setCollectionAccountNumber(String collectionAccountNumber) {
        this.collectionAccountNumber = collectionAccountNumber;
    }
    public void setDisbursementAccountNumber(String disbursementAccountNumber) {
        this.disbursementAccountNumber = disbursementAccountNumber;
    }
    public void setCategory(String category) { this.category = category; }
    public void setTier(String tier) { this.tier = tier; }
    public void setAnnualTurnoverKes(Long annualTurnoverKes) { this.annualTurnoverKes = annualTurnoverKes; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public void setAllowedTenors(List<Integer> allowedTenors) {
        this.allowedTenors = new ArrayList<>(allowedTenors == null ? List.of() : allowedTenors);
    }
}
