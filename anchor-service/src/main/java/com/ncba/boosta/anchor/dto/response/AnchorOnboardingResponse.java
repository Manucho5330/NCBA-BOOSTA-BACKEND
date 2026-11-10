package com.ncba.boosta.anchor.dto.response;

import com.ncba.boosta.anchor.models.AnchorOnboarding;
import com.ncba.boosta.anchor.models.AnchorOnboardingStep;
import java.util.List;

public record AnchorOnboardingResponse(String id, String anchorCode, String companyName,
                                       String branchRegion, String businessManagerName,
                                       String relationshipManagerName, String contactName,
                                       String contactPhone, String contactEmail, String workflowStatus,
                                       AnchorOnboardingStep currentStep, String accountName,
                                       String accountNumber, String collectionAccountNumber,
                                       String disbursementAccountNumber, int partnerTenureMonths,
                                       double baseRate, double margin, double agreedRate,
                                       double facilityFeePct, long facilityLimit, String allowedTenorsCsv,
                                       String ecosystemReviewComments, String ibpsReference,
                                       String createdBy, String createdAt,
                                       String updatedAt, List<DocumentResponse> documents,
                                       List<ApprovalResponse> approvals, List<MilestoneResponse> milestones,
                                       List<ConsentResponse> retailerConsents) {
    public static AnchorOnboardingResponse from(AnchorOnboarding onboarding) {
        return new AnchorOnboardingResponse(onboarding.getId(), onboarding.getAnchorCode(),
                onboarding.getCompanyName(), onboarding.getBranchRegion(), onboarding.getBusinessManagerName(),
                onboarding.getRelationshipManagerName(), onboarding.getContactName(), onboarding.getContactPhone(),
                onboarding.getContactEmail(), onboarding.getWorkflowStatus(), onboarding.getCurrentStep(),
                onboarding.getAccountName(), onboarding.getAccountNumber(), onboarding.getCollectionAccountNumber(),
                onboarding.getDisbursementAccountNumber(), onboarding.getPartnerTenureMonths(),
                onboarding.getBaseRate(), onboarding.getMargin(), onboarding.getAgreedRate(),
                onboarding.getFacilityFeePct(), onboarding.getFacilityLimit(), onboarding.getAllowedTenorsCsv(),
                onboarding.getEcosystemReviewComments(), onboarding.getIbpsReference(), onboarding.getCreatedBy(),
                onboarding.getCreatedAt(), onboarding.getUpdatedAt(),
                onboarding.getDocuments().stream().map(item -> new DocumentResponse(item.getDocumentType(),
                        item.getFileName(), item.getReference(), item.getRecordedAt())).toList(),
                onboarding.getApprovals().stream().map(item -> new ApprovalResponse(item.getStage(),
                        item.getApproverRole(), item.getApproverName(), item.getSignedDate(), item.getDecision(),
                        item.getExternalReference(), item.getComments(), item.getRecordedAt())).toList(),
                onboarding.getMilestones().stream().map(item -> new MilestoneResponse(item.getStep(),
                        item.getCompletedBy(), item.getExternalReference(), item.getComments(),
                        item.getCompletedAt())).toList(),
                onboarding.getRetailerConsents().stream().map(item -> new ConsentResponse(item.getRetailerId(),
                        item.getConsentReference(), item.getRecordedAt())).toList());
    }

    public record DocumentResponse(String documentType, String fileName, String reference, String recordedAt) {
    }

    public record ApprovalResponse(String stage, String approverRole, String approverName, String signedDate,
                                   String decision, String externalReference, String comments, String recordedAt) {
    }

    public record MilestoneResponse(String step, String completedBy, String externalReference,
                                    String comments, String completedAt) {
    }

    public record ConsentResponse(String retailerId, String consentReference, String recordedAt) {
    }
}
