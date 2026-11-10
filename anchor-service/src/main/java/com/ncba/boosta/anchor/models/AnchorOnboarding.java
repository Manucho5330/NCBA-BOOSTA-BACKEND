package com.ncba.boosta.anchor.models;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "anchor_onboardings")
public class AnchorOnboarding {
    @Id
    private String id;
    private String anchorCode;
    private String companyName;
    private String branchRegion;
    private String businessManagerName;
    private String relationshipManagerName;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String workflowStatus;
    @Enumerated(EnumType.STRING)
    private AnchorOnboardingStep currentStep;
    private String accountName;
    private String accountNumber;
    private String collectionAccountNumber;
    private String disbursementAccountNumber;
    private int partnerTenureMonths;
    private double baseRate;
    private double margin;
    private double agreedRate;
    private double facilityFeePct;
    private long facilityLimit;
    private String allowedTenorsCsv;
    private String ecosystemReviewComments;
    private String ibpsReference;
    private String createdBy;
    private String createdById;
    private String createdAt;
    private String updatedAt;

    @ElementCollection
    @CollectionTable(name = "anchor_onboarding_documents", joinColumns = @JoinColumn(name = "onboarding_id"))
    @OrderColumn(name = "document_order")
    private List<AnchorOnboardingDocument> documents = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "anchor_onboarding_approvals", joinColumns = @JoinColumn(name = "onboarding_id"))
    @OrderColumn(name = "approval_order")
    private List<AnchorOnboardingApproval> approvals = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "anchor_onboarding_milestones", joinColumns = @JoinColumn(name = "onboarding_id"))
    @OrderColumn(name = "milestone_order")
    private List<AnchorOnboardingMilestone> milestones = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "anchor_onboarding_consents", joinColumns = @JoinColumn(name = "onboarding_id"))
    @OrderColumn(name = "consent_order")
    private List<AnchorRetailerConsent> retailerConsents = new ArrayList<>();

    protected AnchorOnboarding() {
    }

    public AnchorOnboarding(String id, String anchorCode, String companyName, String branchRegion,
                            String businessManagerName, String relationshipManagerName, String contactName,
                            String contactPhone, String contactEmail, String createdBy, String createdById,
                            String now) {
        this.id = id;
        this.anchorCode = anchorCode;
        this.companyName = companyName;
        this.branchRegion = branchRegion;
        this.businessManagerName = businessManagerName;
        this.relationshipManagerName = relationshipManagerName;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.createdBy = createdBy;
        this.createdById = createdById;
        this.workflowStatus = "IN_PROGRESS";
        this.currentStep = AnchorOnboardingStep.PITCH_AND_PROPOSAL;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public String getId() { return id; }
    public String getAnchorCode() { return anchorCode; }
    public String getCompanyName() { return companyName; }
    public String getBranchRegion() { return branchRegion; }
    public String getBusinessManagerName() { return businessManagerName; }
    public String getRelationshipManagerName() { return relationshipManagerName; }
    public String getContactName() { return contactName; }
    public String getContactPhone() { return contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public String getWorkflowStatus() { return workflowStatus; }
    public AnchorOnboardingStep getCurrentStep() { return currentStep; }
    public String getAccountName() { return accountName; }
    public String getAccountNumber() { return accountNumber; }
    public String getCollectionAccountNumber() { return collectionAccountNumber; }
    public String getDisbursementAccountNumber() { return disbursementAccountNumber; }
    public int getPartnerTenureMonths() { return partnerTenureMonths; }
    public double getBaseRate() { return baseRate; }
    public double getMargin() { return margin; }
    public double getAgreedRate() { return agreedRate; }
    public double getFacilityFeePct() { return facilityFeePct; }
    public long getFacilityLimit() { return facilityLimit; }
    public String getAllowedTenorsCsv() { return allowedTenorsCsv; }
    public String getEcosystemReviewComments() { return ecosystemReviewComments; }
    public String getIbpsReference() { return ibpsReference; }
    public String getCreatedBy() { return createdBy; }
    public String getCreatedById() { return createdById; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public List<AnchorOnboardingDocument> getDocuments() { return List.copyOf(documents); }
    public List<AnchorOnboardingApproval> getApprovals() { return List.copyOf(approvals); }
    public List<AnchorOnboardingMilestone> getMilestones() { return List.copyOf(milestones); }
    public List<AnchorRetailerConsent> getRetailerConsents() { return List.copyOf(retailerConsents); }

    public void addDocument(AnchorOnboardingDocument document) { documents.add(document); }
    public void addApproval(AnchorOnboardingApproval approval) { approvals.add(approval); }
    public void addMilestone(AnchorOnboardingMilestone milestone) { milestones.add(milestone); }
    public void addRetailerConsent(AnchorRetailerConsent consent) { retailerConsents.add(consent); }
    public void setCurrentStep(AnchorOnboardingStep currentStep) { this.currentStep = currentStep; }
    public void setWorkflowStatus(String workflowStatus) { this.workflowStatus = workflowStatus; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setCollectionAccountNumber(String collectionAccountNumber) {
        this.collectionAccountNumber = collectionAccountNumber;
    }
    public void setDisbursementAccountNumber(String disbursementAccountNumber) {
        this.disbursementAccountNumber = disbursementAccountNumber;
    }
    public void setPartnerTenureMonths(int partnerTenureMonths) { this.partnerTenureMonths = partnerTenureMonths; }
    public void setBaseRate(double baseRate) { this.baseRate = baseRate; }
    public void setMargin(double margin) { this.margin = margin; }
    public void setAgreedRate(double agreedRate) { this.agreedRate = agreedRate; }
    public void setFacilityFeePct(double facilityFeePct) { this.facilityFeePct = facilityFeePct; }
    public void setFacilityLimit(long facilityLimit) { this.facilityLimit = facilityLimit; }
    public void setAllowedTenorsCsv(String allowedTenorsCsv) { this.allowedTenorsCsv = allowedTenorsCsv; }
    public void setEcosystemReviewComments(String ecosystemReviewComments) {
        this.ecosystemReviewComments = ecosystemReviewComments;
    }
    public void setIbpsReference(String ibpsReference) { this.ibpsReference = ibpsReference; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
