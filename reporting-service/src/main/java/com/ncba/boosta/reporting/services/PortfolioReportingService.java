package com.ncba.boosta.reporting.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class PortfolioReportingService {
    private final ExternalLmsPortfolioProvider externalLmsPortfolioProvider;

    public PortfolioReportingService(ExternalLmsPortfolioProvider externalLmsPortfolioProvider) {
        this.externalLmsPortfolioProvider = externalLmsPortfolioProvider;
    }

    public JsonNode getPortfolioReport() {
        return externalLmsPortfolioProvider.getPortfolioReport();
    }
}
