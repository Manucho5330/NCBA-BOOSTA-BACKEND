package com.ncba.boosta.customer.controllers;

import com.ncba.boosta.customer.models.Customer;
import com.ncba.boosta.customer.models.Loan;
import com.ncba.boosta.customer.dto.response.AnchorCustomerResponse;
import com.ncba.boosta.customer.dto.response.AnchorTransactionResponse;
import com.ncba.boosta.customer.repository.CustomerRepository;
import com.ncba.boosta.customer.repository.LoanRepository;
import com.ncba.boosta.customer.repository.PortfolioTransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/anchor-data/{anchorId}")
public class AnchorDataController {
    private final CustomerRepository customers;
    private final LoanRepository loans;
    private final PortfolioTransactionRepository transactions;

    public AnchorDataController(CustomerRepository customers, LoanRepository loans,
                                PortfolioTransactionRepository transactions) {
        this.customers = customers;
        this.loans = loans;
        this.transactions = transactions;
    }

    @GetMapping("/customers")
    public List<AnchorCustomerResponse> customers(@PathVariable String anchorId, @AuthenticationPrincipal Jwt jwt) {
        authorize(anchorId, jwt);
        return loans.findByAnchorId(anchorId).stream()
                .map(Loan::getCustomerId)
                .distinct()
                .map(customerId -> customers.findById(customerId)
                        .map(customer -> toAnchorCustomer(customer, anchorId))
                        .orElse(null))
                .filter(customer -> customer != null)
                .toList();
    }

    @GetMapping("/transactions")
    public List<AnchorTransactionResponse> transactions(@PathVariable String anchorId, @AuthenticationPrincipal Jwt jwt) {
        authorize(anchorId, jwt);
        return transactions.findByAnchorId(anchorId).stream()
                .map(transaction -> {
                    String customerName = loans.findById(transaction.getLoanId())
                            .flatMap(loan -> customers.findById(loan.getCustomerId()))
                            .map(Customer::getFullName)
                            .orElse(null);
                    return new AnchorTransactionResponse(transaction.getId(), transaction.getReference(),
                            transaction.getDate(), transaction.getType(), transaction.getAmount(),
                            transaction.getStatus(), customerName);
                })
                .toList();
    }

    private AnchorCustomerResponse toAnchorCustomer(Customer customer, String anchorId) {
        List<Loan> customerLoans = loans.findByAnchorId(anchorId).stream()
                .filter(loan -> customer.getId().equals(loan.getCustomerId()))
                .toList();
        long outstanding = customerLoans.stream().mapToLong(Loan::getOutstandingBalance).sum();
        int daysOverdue = customerLoans.stream()
                .filter(loan -> loan.getOutstandingBalance() > 0)
                .mapToInt(loan -> Math.max(0, (int) (LocalDate.now().toEpochDay()
                        - LocalDate.parse(loan.getDueDate()).toEpochDay())))
                .max().orElse(0);
        String ageing = daysOverdue == 0 ? "Current" : daysOverdue <= 30 ? "1-30 DPD"
                : daysOverdue <= 60 ? "31-60 DPD" : daysOverdue <= 90 ? "61-90 DPD" : "90+ DPD";
        String status = daysOverdue > 30 ? "RESTRICTED" : outstanding == 0 ? "DORMANT" : "ACTIVE";
        String upcomingDue = customerLoans.stream()
                .filter(loan -> loan.getOutstandingBalance() > 0)
                .map(Loan::getDueDate)
                .min(String::compareTo)
                .orElse(null);
        return new AnchorCustomerResponse(customer.getId(), customer.getFullName(),
                customer.getNationalId() == null ? "" : customer.getNationalId(),
                0, 0, 0, outstanding, 0, ageing, daysOverdue, upcomingDue, status);
    }

    private static void authorize(String anchorId, Jwt jwt) {
        if (!"ANCHOR".equals(jwt.getClaimAsString("role"))
                || !anchorId.equals(jwt.getClaimAsString("subjectId"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Anchor data can only be accessed by its linked anchor account.");
        }
    }
}
