package com.ncba.boosta.customer.services;

import com.ncba.boosta.customer.integrations.AnchorServiceClient;
import com.ncba.boosta.customer.integrations.BankingIntegrationClient;
import com.ncba.boosta.customer.integrations.ReconciliationTokenProvider;
import com.ncba.boosta.customer.models.Customer;
import com.ncba.boosta.customer.models.DrawdownRequest;
import com.ncba.boosta.customer.models.RepaymentRequest;
import com.ncba.boosta.customer.repository.CustomerRepository;
import com.ncba.boosta.customer.repository.DrawdownRequestRepository;
import com.ncba.boosta.customer.repository.RepaymentRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class DrawdownReconciliationService {
    private static final Logger logger = LoggerFactory.getLogger(DrawdownReconciliationService.class);

    private final DrawdownRequestRepository drawdowns;
    private final CustomerRepository customers;
    private final AnchorServiceClient anchors;
    private final BankingIntegrationClient banking;
    private final ReconciliationTokenProvider tokens;
    private final DrawdownFinalizationService finalizer;
    private final RepaymentRequestRepository repayments;
    private final RepaymentFinalizationService repaymentFinalizer;

    public DrawdownReconciliationService(DrawdownRequestRepository drawdowns, CustomerRepository customers,
                                         AnchorServiceClient anchors, BankingIntegrationClient banking,
                                         ReconciliationTokenProvider tokens,
                                         DrawdownFinalizationService finalizer,
                                         RepaymentRequestRepository repayments,
                                         RepaymentFinalizationService repaymentFinalizer) {
        this.drawdowns = drawdowns;
        this.customers = customers;
        this.anchors = anchors;
        this.banking = banking;
        this.tokens = tokens;
        this.finalizer = finalizer;
        this.repayments = repayments;
        this.repaymentFinalizer = repaymentFinalizer;
    }

    @Scheduled(fixedDelayString = "${drawdowns.confirmation-poll-interval:30000}")
    public void reconcilePendingDrawdowns() {
        List<DrawdownRequest> pending = drawdowns.findByStatusIn(
                List.of("PENDING", "AWAITING_CONFIRMATION"));
        List<RepaymentRequest> pendingRepayments = repayments.findByStatusIn(
                List.of("PENDING", "AWAITING_CONFIRMATION"));
        if (pending.isEmpty() && pendingRepayments.isEmpty()) return;

        String token = tokens.getBearerToken();
        for (DrawdownRequest drawdown : pending) {
            try {
                reconcile(drawdown, token);
            } catch (WebClientRequestException | WebClientResponseException exception) {
                logger.error("Could not reconcile drawdown {} with a downstream service.",
                        drawdown.getReference(), exception);
            }
        }
        for (RepaymentRequest repayment : pendingRepayments) {
            try {
                reconcileRepayment(repayment, token);
            } catch (WebClientRequestException | WebClientResponseException exception) {
                logger.error("Could not reconcile repayment {} with a downstream service.",
                        repayment.getReference(), exception);
            }
        }
    }

    private void reconcile(DrawdownRequest drawdown, String authorization) {
        if ("PENDING".equals(drawdown.getStatus())) {
            initiate(drawdown, authorization);
            return;
        }
        BankingIntegrationClient.TransactionResult confirmation =
                banking.confirmation(drawdown.getBankingReference(), authorization);
        if (!drawdown.getBankingReference().equals(confirmation.transactionReference())) {
            throw new IllegalStateException("Banking confirmation reference does not match the drawdown.");
        }
        String status = normalize(confirmation.status());
        if (isConfirmed(status)) {
            finalizer.disburse(drawdown.getId(), confirmation.transactionReference(), confirmation.confirmedAt());
        } else if (isRejected(status)) {
            anchors.release(drawdown.getAnchorId(), drawdown.getId(), authorization);
            drawdown.setStatus("DECLINED");
            drawdown.setFailureReason("CORE_BANKING_" + status);
            drawdown.setUpdatedAt(Instant.now().toString());
            drawdowns.save(drawdown);
        }
    }

    private void initiate(DrawdownRequest drawdown, String authorization) {
        Customer customer = customers.findById(drawdown.getCustomerId())
                .orElseThrow(() -> new IllegalStateException("Customer does not exist."));
        AnchorServiceClient.AnchorRecord anchor;
        try {
            anchor = anchors.getAnchor(drawdown.getAnchorId(), authorization);
        } catch (WebClientResponseException.NotFound exception) {
            decline(drawdown, "ANCHOR_NOT_FOUND");
            logger.warn("Declined drawdown {} because anchor {} was not found.",
                    drawdown.getReference(), drawdown.getAnchorId());
            return;
        }
        if (!anchor.drawdownAllowed() || !anchor.allowsTenor(drawdown.getTenorDays())) {
            decline(drawdown, "ANCHOR_POLICY_REJECTED");
            return;
        }
        if (anchor.disbursementAccountNumber() == null || anchor.disbursementAccountNumber().isBlank()) {
            decline(drawdown, "ANCHOR_DISBURSEMENT_ACCOUNT_NOT_CONFIGURED");
            return;
        }
        if (customer.getNcbaAccountNumber() == null || customer.getNcbaAccountNumber().isBlank()) {
            decline(drawdown, "CUSTOMER_NCBA_ACCOUNT_NOT_CONFIGURED");
            return;
        }

        try {
            anchors.reserve(drawdown.getAnchorId(), drawdown.getId(), drawdown.getAmount(), authorization);
        } catch (WebClientResponseException.Conflict exception) {
            decline(drawdown, "ANCHOR_LIMIT_UNAVAILABLE");
            logger.warn("Declined drawdown {} because its anchor limit could not be reserved.",
                    drawdown.getReference(), exception);
            return;
        }
        BankingIntegrationClient.TransactionResult result = banking.initiate(
                new BankingIntegrationClient.TransactionRequest(drawdown.getCustomerId(),
                        anchor.disbursementAccountNumber(), customer.getNcbaAccountNumber(), drawdown.getAmount(),
                        "KES", drawdown.getId(), "BOO drawdown " + drawdown.getReference()),
                authorization);
        String status = normalize(result.status());
        if (isRejected(status)) {
            anchors.release(drawdown.getAnchorId(), drawdown.getId(), authorization);
            decline(drawdown, "CORE_BANKING_" + status);
            return;
        }
        if (result.transactionReference() == null || result.transactionReference().isBlank()) {
            throw new IllegalStateException("Core banking initiation returned no transaction reference.");
        }
        drawdown.setBankingReference(result.transactionReference());
        drawdown.setStatus("AWAITING_CONFIRMATION");
        drawdown.setUpdatedAt(Instant.now().toString());
        drawdowns.save(drawdown);
    }

    private void decline(DrawdownRequest request, String reason) {
        request.setStatus("DECLINED");
        request.setFailureReason(reason);
        request.setUpdatedAt(Instant.now().toString());
        drawdowns.save(request);
    }

    private void reconcileRepayment(RepaymentRequest request, String authorization) {
        if ("PENDING".equals(request.getStatus())) {
            initiateRepayment(request, authorization);
            return;
        }
        BankingIntegrationClient.TransactionResult confirmation =
                banking.confirmation(request.getBankingReference(), authorization);
        if (!request.getBankingReference().equals(confirmation.transactionReference())) {
            throw new IllegalStateException("Banking confirmation reference does not match the repayment.");
        }
        String status = normalize(confirmation.status());
        if (isConfirmed(status)) {
            anchors.creditLimit(request.getAnchorId(), request.getId(), request.getAmount(), authorization);
            repaymentFinalizer.complete(request.getId(), confirmation.transactionReference());
        } else if (isRejected(status)) {
            request.setStatus("DECLINED");
            request.setFailureReason("CORE_BANKING_" + status);
            request.setUpdatedAt(Instant.now().toString());
            repayments.save(request);
        }
    }

    private void initiateRepayment(RepaymentRequest request, String authorization) {
        Customer customer = customers.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalStateException("Repayment customer does not exist."));
        if (customer.getNcbaAccountNumber() == null || customer.getNcbaAccountNumber().isBlank()) {
            request.setStatus("DECLINED");
            request.setFailureReason("CUSTOMER_NCBA_ACCOUNT_NOT_CONFIGURED");
            request.setUpdatedAt(Instant.now().toString());
            repayments.save(request);
            return;
        }
        AnchorServiceClient.AnchorRecord anchor;
        try {
            anchor = anchors.getAnchor(request.getAnchorId(), authorization);
        } catch (WebClientResponseException.NotFound exception) {
            request.setStatus("DECLINED");
            request.setFailureReason("ANCHOR_NOT_FOUND");
            request.setUpdatedAt(Instant.now().toString());
            repayments.save(request);
            logger.warn("Declined repayment {} because anchor {} was not found.",
                    request.getReference(), request.getAnchorId());
            return;
        }
        if (anchor.collectionAccountNumber() == null || anchor.collectionAccountNumber().isBlank()) {
            request.setStatus("DECLINED");
            request.setFailureReason("ANCHOR_COLLECTION_ACCOUNT_NOT_CONFIGURED");
            request.setUpdatedAt(Instant.now().toString());
            repayments.save(request);
            return;
        }
        BankingIntegrationClient.TransactionResult result = banking.initiate(
                new BankingIntegrationClient.TransactionRequest(request.getCustomerId(),
                        customer.getNcbaAccountNumber(), anchor.collectionAccountNumber(), request.getAmount(),
                        "KES", request.getId(), "BOO repayment " + request.getReference()),
                authorization);
        String status = normalize(result.status());
        if (isRejected(status)) {
            request.setStatus("DECLINED");
            request.setFailureReason("CORE_BANKING_" + status);
            request.setUpdatedAt(Instant.now().toString());
            repayments.save(request);
            return;
        }
        if (result.transactionReference() == null || result.transactionReference().isBlank()) {
            throw new IllegalStateException("Core banking initiation returned no transaction reference.");
        }
        request.setBankingReference(result.transactionReference());
        request.setStatus("AWAITING_CONFIRMATION");
        request.setUpdatedAt(Instant.now().toString());
        repayments.save(request);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static boolean isConfirmed(String status) {
        return List.of("COMPLETED", "SUCCESS", "SUCCESSFUL", "CONFIRMED").contains(status);
    }

    private static boolean isRejected(String status) {
        return List.of("FAILED", "REJECTED", "DECLINED", "CANCELLED").contains(status);
    }
}
