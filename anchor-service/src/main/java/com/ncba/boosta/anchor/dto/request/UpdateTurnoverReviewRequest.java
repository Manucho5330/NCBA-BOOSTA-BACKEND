package com.ncba.boosta.anchor.dto.request;

import java.util.List;

public record UpdateTurnoverReviewRequest(String status, List<String> documents,
                                          String reviewerComments, Long newProposedLimit) {
}
