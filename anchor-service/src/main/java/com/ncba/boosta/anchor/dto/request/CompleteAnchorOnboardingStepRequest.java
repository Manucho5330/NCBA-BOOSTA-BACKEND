package com.ncba.boosta.anchor.dto.request;

import java.util.List;

public record CompleteAnchorOnboardingStepRequest(String reference, String comments,
                                                  String ecosystemOutcome, String mouOutcome,
                                                  List<DocumentInput> documents,
                                                  List<ApprovalInput> approvals,
                                                  AnchorFormInput anchorForm,
                                                  List<ConsentInput> consents) {
    public static CompleteAnchorOnboardingStepRequest empty() {
        return new CompleteAnchorOnboardingStepRequest(null, null, null, null, null, null, null, null);
    }

    public record DocumentInput(String documentType, String fileName, String reference) {
    }

    public record ApprovalInput(String approverRole, String approverName, String signedDate, String decision,
                                String externalReference, String comments) {
    }

    public record AnchorFormInput(String accountName, String accountNumber, String collectionAccountNumber,
                                  String disbursementAccountNumber, int partnerTenureMonths, double baseRate,
                                  double margin, double agreedRate, double facilityFeePct, long facilityLimit,
                                  List<Integer> allowedTenors) {
    }

    public record ConsentInput(String retailerId, String consentReference) {
    }
}
