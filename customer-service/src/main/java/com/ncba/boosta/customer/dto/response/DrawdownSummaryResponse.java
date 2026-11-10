package com.ncba.boosta.customer.dto.response;

import java.util.List;

public record DrawdownSummaryResponse(String id, String reference, String anchorId, long amount,
                                      int tenorDays, String purpose, String status, String initiatedBy,
                                      String mandateType, List<Object> approvals, String createdAt,
                                      String decidedAt, String loanId) {
}
