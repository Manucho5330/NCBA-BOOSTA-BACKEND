package com.ncba.boosta.anchor.dto.response;

public record AnchorPortalCustomerResponse(String customerId, String name, String nationalId,
                                           long monthlySales, long annualSales, long limit,
                                           long outstanding, double utilisationPct, String overdueAgeing,
                                           int daysOverdue, String upcomingDueDate, String status) {
}
