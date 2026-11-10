package com.ncba.boosta.anchor.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AnchorOnboardingMilestone {
    @Column(name = "step_name")
    private String step;
    @Column(name = "completed_by")
    private String completedBy;
    @Column(name = "external_reference")
    private String externalReference;
    @Column(name = "comments", length = 2000)
    private String comments;
    @Column(name = "completed_at")
    private String completedAt;

    protected AnchorOnboardingMilestone() {
    }

    public AnchorOnboardingMilestone(String step, String completedBy, String externalReference,
                                     String comments, String completedAt) {
        this.step = step;
        this.completedBy = completedBy;
        this.externalReference = externalReference;
        this.comments = comments;
        this.completedAt = completedAt;
    }

    public String getStep() { return step; }
    public String getCompletedBy() { return completedBy; }
    public String getExternalReference() { return externalReference; }
    public String getComments() { return comments; }
    public String getCompletedAt() { return completedAt; }
}
