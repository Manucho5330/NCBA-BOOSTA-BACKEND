package com.ncba.boosta.customer.dto.response;

public record AnchorCustomerResponse(String customerId, String name, String nationalId,
                                     long monthlySales, long annualSales, long limit, long outstanding,
                                     double utilisationPct, String overdueAgeing, int daysOverdue,
                                     String upcomingDueDate, String status) {
}
