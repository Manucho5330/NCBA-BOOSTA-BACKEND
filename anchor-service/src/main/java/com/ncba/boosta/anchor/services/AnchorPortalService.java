package com.ncba.boosta.anchor.services;

import com.ncba.boosta.anchor.integrations.CustomerDomainClient;
import com.ncba.boosta.anchor.models.Anchor;
import com.ncba.boosta.anchor.models.AnchorTurnoverReview;
import com.ncba.boosta.anchor.repository.AnchorRepository;
import com.ncba.boosta.anchor.repository.AnchorTurnoverReviewRepository;
import com.ncba.boosta.anchor.dto.request.UpdateTurnoverReviewRequest;
import com.ncba.boosta.anchor.dto.response.AnchorPortalCustomerResponse;
import com.ncba.boosta.anchor.dto.response.AnchorPortalTransactionResponse;
import com.ncba.boosta.anchor.dto.response.AnchorPortalUserResponse;
import com.ncba.boosta.anchor.dto.response.SalesPointResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class AnchorPortalService {
    private final AnchorRepository anchors;
    private final AnchorTurnoverReviewRepository reviews;
    private final CustomerDomainClient customerDomain;

    public AnchorPortalService(AnchorRepository anchors, AnchorTurnoverReviewRepository reviews,
                               CustomerDomainClient customerDomain) {
        this.anchors = anchors;
        this.reviews = reviews;
        this.customerDomain = customerDomain;
    }

    public AnchorPortalUserResponse getUser(String anchorId) {
        Anchor anchor = anchor(anchorId);
        return new AnchorPortalUserResponse(anchor.getId(), anchor.getName(),
                valueOrEmpty(anchor.getContactPerson()), valueOrEmpty(anchor.getContactEmail()), "ANCHOR");
    }

    public List<AnchorPortalCustomerResponse> getCustomers(String anchorId, String authorization) {
        Anchor anchor = anchor(anchorId);
        return customerDomain.customers(anchorId, authorization).stream()
                .map(customer -> new AnchorPortalCustomerResponse(customer.customerId(), customer.name(), customer.nationalId(),
                        customer.monthlySales(), customer.annualSales(), anchor.getApprovedLimit(),
                        customer.outstanding(), anchor.getApprovedLimit() == 0 ? 0
                        : Math.round((double) customer.outstanding() / anchor.getApprovedLimit() * 1000) / 10.0,
                        customer.overdueAgeing(), customer.daysOverdue(), customer.upcomingDueDate(),
                        customer.status()))
                .toList();
    }

    public List<SalesPointResponse> getSales(String anchorId, String period, String authorization) {
        anchor(anchorId);
        int count = switch (period) {
            case "weekly" -> 8;
            case "monthly" -> 12;
            case "annual" -> 3;
            default -> throw new IllegalArgumentException("Sales period must be weekly, monthly, or annual.");
        };
        LocalDate today = LocalDate.now();
        List<CustomerDomainClient.AnchorTransaction> transactions =
                customerDomain.transactions(anchorId, authorization);
        List<SalesPointResponse> points = new ArrayList<>();
        for (int offset = count - 1; offset >= 0; offset--) {
            LocalDate start;
            LocalDate end;
            String label;
            if ("weekly".equals(period)) {
                end = today.minusWeeks(offset);
                start = end.minusDays(6);
                label = "Week of " + start;
            } else if ("monthly".equals(period)) {
                YearMonth month = YearMonth.from(today).minusMonths(offset);
                start = month.atDay(1);
                end = month.atEndOfMonth();
                label = month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            } else {
                int year = today.getYear() - offset;
                start = LocalDate.of(year, 1, 1);
                end = LocalDate.of(year, 12, 31);
                label = Integer.toString(year);
            }
            long sales = transactions.stream()
                    .filter(transaction -> "DISBURSEMENT".equals(transaction.type()))
                    .filter(transaction -> {
                        LocalDate date = LocalDate.parse(transaction.date());
                        return !date.isBefore(start) && !date.isAfter(end);
                    })
                    .mapToLong(CustomerDomainClient.AnchorTransaction::amount)
                    .sum();
            points.add(new SalesPointResponse(label, sales));
        }
        return points;
    }

    public AnchorTurnoverReview getTurnoverReview(String anchorId) {
        anchor(anchorId);
        return reviews.findById(anchorId).orElseGet(() -> {
            LocalDate today = LocalDate.now();
            return reviews.save(new AnchorTurnoverReview(anchorId, anchorId, "FY" + today.getYear(),
                    "NOT_STARTED", LocalDate.of(today.getYear(), 12, 31).toString(),
                    List.of(), today.toString()));
        });
    }

    @Transactional
    public AnchorTurnoverReview updateTurnoverReview(String anchorId, UpdateTurnoverReviewRequest update) {
        AnchorTurnoverReview review = getTurnoverReview(anchorId);
        if (update.status() == null || update.status().isBlank()) {
            throw new IllegalArgumentException("Turnover review status is required.");
        }
        review.setStatus(update.status());
        if (update.documents() != null) review.setDocuments(update.documents());
        if (update.reviewerComments() != null) review.setReviewerComments(update.reviewerComments());
        if (update.newProposedLimit() != null) review.setNewProposedLimit(update.newProposedLimit());
        String now = Instant.now().toString();
        review.setUpdatedAt(now);
        if ("SUBMITTED".equals(update.status()) && review.getSubmittedAt() == null) {
            review.setSubmittedAt(now);
        }
        return reviews.save(review);
    }

    public List<AnchorPortalTransactionResponse> getTransactions(String anchorId, String authorization) {
        anchor(anchorId);
        return customerDomain.transactions(anchorId, authorization).stream()
                .map(transaction -> {
                    String type = switch (transaction.type()) {
                        case "DISBURSEMENT" -> "DISBURSEMENT";
                        case "FEE", "PENALTY" -> "FEES";
                        default -> "REPAYMENT";
                    };
                    return new AnchorPortalTransactionResponse(transaction.id(), transaction.reference(), transaction.date(),
                            type, transaction.amount(), transaction.status(), transaction.customerName());
                })
                .toList();
    }

    private Anchor anchor(String id) {
        return anchors.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anchor not found."));
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

}
