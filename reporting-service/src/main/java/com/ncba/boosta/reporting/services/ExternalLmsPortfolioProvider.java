package com.ncba.boosta.reporting.services;

import com.fasterxml.jackson.databind.JsonNode;

public interface ExternalLmsPortfolioProvider {
    JsonNode getPortfolioReport();
}
