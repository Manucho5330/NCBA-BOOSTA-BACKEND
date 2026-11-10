package com.ncba.boosta.anchor.dto.response;

import com.ncba.boosta.anchor.models.AnchorTurnoverReview;
import java.util.List;

public record TurnoverReviewResponse(String id, String anchorId, String period, String status,
                                     String dueDate, String reviewerComments, Long newProposedLimit,
                                     String submittedAt, String updatedAt, List<String> documents) {
    public static TurnoverReviewResponse from(AnchorTurnoverReview review) {
        return new TurnoverReviewResponse(review.getId(), review.getAnchorId(), review.getPeriod(),
                review.getStatus(), review.getDueDate(), review.getReviewerComments(),
                review.getNewProposedLimit(), review.getSubmittedAt(), review.getUpdatedAt(),
                review.getDocuments());
    }
}
