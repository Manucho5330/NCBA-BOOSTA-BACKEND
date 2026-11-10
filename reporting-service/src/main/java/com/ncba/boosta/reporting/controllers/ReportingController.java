package com.ncba.boosta.reporting.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.ncba.boosta.reporting.services.PortfolioReportingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportingController {
    private final PortfolioReportingService reportingService;

    public ReportingController(PortfolioReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/portfolio")
    public JsonNode portfolio() {
        return reportingService.getPortfolioReport();
    }
}
