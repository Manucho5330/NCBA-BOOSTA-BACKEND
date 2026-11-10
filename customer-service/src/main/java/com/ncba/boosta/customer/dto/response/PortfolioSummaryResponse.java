package com.ncba.boosta.customer.dto.response;

public record PortfolioSummaryResponse(long customerCount, long anchorCount, long loanCount,
                                       long outstandingBalance, long activeLoanCount) {
}
