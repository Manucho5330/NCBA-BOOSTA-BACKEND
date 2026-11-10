package com.ncba.boosta.anchor.models;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "anchor_turnover_reviews")
public class AnchorTurnoverReview {
    @Id
    private String id;
    private String anchorId;
    private String period;
    private String status;
    private String dueDate;
    private String reviewerComments;
    private Long newProposedLimit;
    private String submittedAt;
    private String updatedAt;
    @ElementCollection
    private List<String> documents = new ArrayList<>();

    protected AnchorTurnoverReview() {
    }

    public AnchorTurnoverReview(String id, String anchorId, String period, String status,
                                String dueDate, List<String> documents, String updatedAt) {
        this.id = id;
        this.anchorId = anchorId;
        this.period = period;
        this.status = status;
        this.dueDate = dueDate;
        this.documents = new ArrayList<>(documents);
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public String getAnchorId() { return anchorId; }
    public String getPeriod() { return period; }
    public String getStatus() { return status; }
    public String getDueDate() { return dueDate; }
    public String getReviewerComments() { return reviewerComments; }
    public Long getNewProposedLimit() { return newProposedLimit; }
    public String getSubmittedAt() { return submittedAt; }
    public String getUpdatedAt() { return updatedAt; }
    public List<String> getDocuments() { return List.copyOf(documents); }
    public void setStatus(String status) { this.status = status; }
    public void setDocuments(List<String> documents) {
        this.documents = new ArrayList<>(documents == null ? List.of() : documents);
    }
    public void setReviewerComments(String reviewerComments) { this.reviewerComments = reviewerComments; }
    public void setNewProposedLimit(Long newProposedLimit) { this.newProposedLimit = newProposedLimit; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
