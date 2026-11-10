package com.ncba.boosta.customer.controllers;

import com.ncba.boosta.customer.models.Customer;
import com.ncba.boosta.customer.dto.request.CustomerRequest;
import com.ncba.boosta.customer.dto.response.CustomerResponse;
import com.ncba.boosta.customer.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerRepository customers;

    public CustomerController(CustomerRepository customers) {
        this.customers = customers;
    }

    @GetMapping
    public List<CustomerResponse> list() {
        return customers.findAll().stream().map(CustomerResponse::from).toList();
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        if ("CUSTOMER".equals(jwt.getClaimAsString("role"))
                && !id.equals(jwt.getClaimAsString("subjectId"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customers may only read their own profile.");
        }
        return customers.findById(id)
                .map(CustomerResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found."));
    }

    @PostMapping
    public CustomerResponse create(@RequestBody CustomerRequest request) {
        Customer customer = request.toModel();
        if (customer.getId() == null || customer.getId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer id is required.");
        }
        if (customers.existsById(customer.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer already exists.");
        }
        return CustomerResponse.from(customers.save(customer));
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable String id, @RequestBody CustomerRequest request) {
        Customer update = request.toModel();
        if (update.getId() != null && !id.equals(update.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer id must match the URL.");
        }
        Customer current = customers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found."));
        if (update.getFullName() != null) current.setFullName(update.getFullName());
        if (update.getPhone() != null) current.setPhone(update.getPhone());
        if (update.getEmail() != null) current.setEmail(update.getEmail());
        if (update.getNcbaAccountNumber() != null) current.setNcbaAccountNumber(update.getNcbaAccountNumber());
        if (update.getNationalId() != null) current.setNationalId(update.getNationalId());
        if (update.getBusinessName() != null) current.setBusinessName(update.getBusinessName());
        if (update.getBusinessType() != null) current.setBusinessType(update.getBusinessType());
        if (update.getKycStatus() != null) current.setKycStatus(update.getKycStatus());
        if (update.getMemberSince() != null) current.setMemberSince(update.getMemberSince());
        if (!update.getPrefilledFields().isEmpty()) current.setPrefilledFields(update.getPrefilledFields());
        return CustomerResponse.from(customers.save(current));
    }
}
