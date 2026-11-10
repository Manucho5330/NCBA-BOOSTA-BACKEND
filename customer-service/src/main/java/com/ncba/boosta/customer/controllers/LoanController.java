package com.ncba.boosta.customer.controllers;

import com.ncba.boosta.customer.models.Loan;
import com.ncba.boosta.customer.dto.request.LoanRequest;
import com.ncba.boosta.customer.dto.response.LoanResponse;
import com.ncba.boosta.customer.repository.CustomerRepository;
import com.ncba.boosta.customer.repository.LoanRepository;
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
@RequestMapping("/api/v1/loans")
public class LoanController {
    private final LoanRepository loans;
    private final CustomerRepository customers;

    public LoanController(LoanRepository loans, CustomerRepository customers) {
        this.loans = loans;
        this.customers = customers;
    }

    @GetMapping
    public List<LoanResponse> list(@AuthenticationPrincipal Jwt jwt) {
        List<Loan> results = "CUSTOMER".equals(jwt.getClaimAsString("role"))
                ? loans.findByCustomerId(jwt.getClaimAsString("subjectId"))
                : loans.findAll();
        return results.stream().map(LoanResponse::from).toList();
    }

    @GetMapping("/{id}")
    public LoanResponse get(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        Loan loan = loans.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found."));
        if ("CUSTOMER".equals(jwt.getClaimAsString("role"))
                && !loan.getCustomerId().equals(jwt.getClaimAsString("subjectId"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customers may only read their own loans.");
        }
        return LoanResponse.from(loan);
    }

    @PostMapping
    public LoanResponse create(@RequestBody LoanRequest request) {
        Loan loan = request.toModel();
        if (loan.getId() == null || loan.getId().isBlank()
                || loan.getCustomerId() == null || loan.getCustomerId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loan and customer ids are required.");
        }
        if (!customers.existsById(loan.getCustomerId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loan customer does not exist.");
        }
        if (loans.existsById(loan.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Loan already exists.");
        }
        if (loan.getPrincipal() <= 0 || loan.getOutstandingBalance() < 0
                || loan.getOutstandingBalance() > loan.getPrincipal()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loan balances are invalid.");
        }
        return LoanResponse.from(loans.save(loan));
    }

    @PutMapping("/{id}")
    public LoanResponse update(@PathVariable String id, @RequestBody LoanRequest request) {
        Loan update = request.toModel();
        if (update.getId() != null && !id.equals(update.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loan id must match the URL.");
        }
        Loan current = loans.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found."));
        if (update.getOutstandingBalance() < 0 || update.getOutstandingBalance() > current.getPrincipal()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loan balance is invalid.");
        }
        current.setOutstandingBalance(update.getOutstandingBalance());
        if (update.getStatus() != null) current.setStatus(update.getStatus());
        return LoanResponse.from(loans.save(current));
    }

}
