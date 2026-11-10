package com.ncba.boosta.customer.controllers;

import com.ncba.boosta.customer.integrations.AnchorServiceClient;
import com.ncba.boosta.customer.dto.response.PortfolioSummaryResponse;
import com.ncba.boosta.customer.models.Loan;
import com.ncba.boosta.customer.repository.CustomerRepository;
import com.ncba.boosta.customer.repository.LoanRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PortfolioSummaryController {
    private final CustomerRepository customers;
    private final LoanRepository loans;
    private final AnchorServiceClient anchors;

    public PortfolioSummaryController(CustomerRepository customers, LoanRepository loans,
                                      AnchorServiceClient anchors) {
        this.customers = customers;
        this.loans = loans;
        this.anchors = anchors;
    }

    @GetMapping("/api/v1/portfolio/summary")
    public PortfolioSummaryResponse summary(@AuthenticationPrincipal Jwt jwt) {
        List<Loan> records = loans.findAll();
        long outstanding = records.stream().mapToLong(Loan::getOutstandingBalance).sum();
        long active = records.stream()
                .filter(loan -> "ACTIVE".equals(loan.getStatus()) || "OVERDUE".equals(loan.getStatus()))
                .count();
        return new PortfolioSummaryResponse(customers.count(),
                anchors.countAnchors("Bearer " + jwt.getTokenValue()),
                records.size(), outstanding, active);
    }
}
