package com.ncba.boosta.anchor.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AnchorOnboardingApproval {
    @Column(name = "approval_stage")
    private String stage;
    @Column(name = "approver_role")
    private String approverRole;
    @Column(name = "approver_name")
    private String approverName;
    @Column(name = "signed_date")
    private String signedDate;
    @Column(name = "approval_decision")
    private String decision;
    @Column(name = "approval_reference")
    private String externalReference;
    @Column(name = "approval_comments", length = 2000)
    private String comments;
    @Column(name = "recorded_at")
    private String recordedAt;

    protected AnchorOnboardingApproval() {
    }

    public AnchorOnboardingApproval(String stage, String approverRole, String approverName, String signedDate,
                                   String decision, String externalReference, String comments, String recordedAt) {
        this.stage = stage;
        this.approverRole = approverRole;
        this.approverName = approverName;
        this.signedDate = signedDate;
        this.decision = decision;
        this.externalReference = externalReference;
        this.comments = comments;
        this.recordedAt = recordedAt;
    }

    public String getStage() { return stage; }
    public String getApproverRole() { return approverRole; }
    public String getApproverName() { return approverName; }
    public String getSignedDate() { return signedDate; }
    public String getDecision() { return decision; }
    public String getExternalReference() { return externalReference; }
    public String getComments() { return comments; }
    public String getRecordedAt() { return recordedAt; }
}
