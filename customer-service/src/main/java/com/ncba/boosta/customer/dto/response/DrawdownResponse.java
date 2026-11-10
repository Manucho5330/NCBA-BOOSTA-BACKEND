package com.ncba.boosta.customer.dto.response;

public record DrawdownResponse(String status, DrawdownSummaryResponse drawdownRequest,
                               LoanResponse loan, String declineReason) {
}
