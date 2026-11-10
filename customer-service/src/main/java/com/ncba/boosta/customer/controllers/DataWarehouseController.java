package com.ncba.boosta.customer.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.ncba.boosta.customer.integrations.DataWarehouseClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customer-data-warehouse")
public class DataWarehouseController {
    private final DataWarehouseClient dataWarehouse;

    public DataWarehouseController(DataWarehouseClient dataWarehouse) {
        this.dataWarehouse = dataWarehouse;
    }

    @GetMapping("/customers/{customerId}")
    public JsonNode getCustomerProfile(@PathVariable String customerId) {
        return dataWarehouse.getCustomerProfile(customerId);
    }
}
