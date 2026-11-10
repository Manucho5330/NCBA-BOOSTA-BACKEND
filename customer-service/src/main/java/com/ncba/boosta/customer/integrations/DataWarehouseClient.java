package com.ncba.boosta.customer.integrations;

import com.fasterxml.jackson.databind.JsonNode;

public interface DataWarehouseClient {
    JsonNode getCustomerProfile(String customerId);
}
