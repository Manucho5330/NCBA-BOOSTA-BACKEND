package com.ncba.boosta.anchor.services;

import com.ncba.boosta.anchor.models.Anchor;
import com.ncba.boosta.anchor.models.AnchorOnboarding;
import com.ncba.boosta.anchor.models.AnchorOnboardingApproval;
import com.ncba.boosta.anchor.models.AnchorOnboardingDocument;
import com.ncba.boosta.anchor.models.AnchorOnboardingMilestone;
import com.ncba.boosta.anchor.models.AnchorOnboardingStep;
import com.ncba.boosta.anchor.models.AnchorRetailerConsent;
import com.ncba.boosta.anchor.repository.AnchorOnboardingRepository;
import com.ncba.boosta.anchor.repository.AnchorRepository;
import com.ncba.boosta.anchor.dto.request.CreateAnchorOnboardingRequest;
import com.ncba.boosta.anchor.dto.request.CompleteAnchorOnboardingStepRequest;
import com.ncba.boosta.anchor.dto.request.CompleteAnchorOnboardingStepRequest.ApprovalInput;
import com.ncba.boosta.anchor.dto.request.CompleteAnchorOnboardingStepRequest.AnchorFormInput;
import com.ncba.boosta.anchor.dto.request.CompleteAnchorOnboardingStepRequest.ConsentInput;
import com.ncba.boosta.anchor.dto.request.CompleteAnchorOnboardingStepRequest.DocumentInput;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AnchorOnboardingService {
    private static final Set<String> INITIAL_DOCUMENTS = Set.of(
            "AUDITED_FINANCIALS_3Y", "DEBTOR_CREDITOR_AGEING", "DIRECTOR_ID_COPIES",
            "DIRECTOR_KRA_PINS", "DIRECTOR_CRB_CERTIFICATES", "COMPANY_CR8", "COMPANY_CR12",
            "COMPANY_MEMORANDUM_ARTICLES");
    private static final List<String> CR_APPROVERS = List.of(
            "BRANCH_REGION_RM_ECOSYSTEM_CPA",
            "BM_HOD",
            "CSME_ECOSYSTEMS",
            "CSME_CREDIT_ANALYST",
            "HEAD_RETAIL_CREDIT_SOLUTIONS",
            "HEAD_SME_BANKING",
            "DIRECTOR_COMMERCIAL_SME_BANKING",
            "SENIOR_MANAGER_CSME_APPROVALS",
            "DIRECTOR_CRM",
            "AGRD",
            "GDCRM");
    private static final List<String> CAR_APPROVERS = List.of(
            "BM_RM", "ECOSYSTEM_TEAM", "CONSUMER_ANALYSTS", "CRM", "RETAIL_MANAGEMENT");

    private final AnchorOnboardingRepository onboardings;
    private final AnchorRepository anchors;

    public AnchorOnboardingService(AnchorOnboardingRepository onboardings, AnchorRepository anchors) {
        this.onboardings = onboardings;
        this.anchors = anchors;
    }

    public List<AnchorOnboarding> list(String actorId, boolean reviewer) {
        return reviewer ? onboardings.findAll() : onboardings.findAllByCreatedById(actorId);
    }

    public AnchorOnboarding get(String id, String actorId, boolean reviewer) {
        AnchorOnboarding onboarding = onboardings.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anchor onboarding not found."));
        if (!reviewer && !actorId.equals(onboarding.getCreatedById())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Anchor onboarding not found.");
        }
        return onboarding;
    }

    @Transactional
    public AnchorOnboarding create(CreateAnchorOnboardingRequest request, String actor, String actorId) {
        requireText(request.anchorCode(), "Anchor code is required.");
        requireText(request.companyName(), "Company name is required.");
        requireText(request.branchRegion(), "Branch or region is required.");
        requireText(request.businessManagerName(), "Business Manager name is required.");
        requireText(request.relationshipManagerName(), "Relationship Manager name is required.");
        if (onboardings.existsByAnchorCodeIgnoreCase(request.anchorCode().trim())
                || anchors.existsById(request.anchorCode().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Anchor code is already in use.");
        }
        String now = Instant.now().toString();
        return onboardings.save(new AnchorOnboarding(UUID.randomUUID().toString(),
                request.anchorCode().trim(), request.companyName().trim(), request.branchRegion().trim(),
                request.businessManagerName().trim(), request.relationshipManagerName().trim(),
                valueOrEmpty(request.contactName()), valueOrEmpty(request.contactPhone()),
                valueOrEmpty(request.contactEmail()), actor, actorId, now));
    }

    @Transactional
    public AnchorOnboarding completeStep(String id, AnchorOnboardingStep step,
                                         CompleteAnchorOnboardingStepRequest request, String actor, String actorId,
                                         boolean reviewer) {
        AnchorOnboarding onboarding = onboardings.findLockedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Anchor onboarding not found."));
        if (!reviewer && !actorId.equals(onboarding.getCreatedById())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Anchor onboarding not found.");
        }
        if (onboarding.getCurrentStep() != step) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Complete the current workflow step before submitting another step.");
        }
        onboarding.setWorkflowStatus("IN_PROGRESS");
        String now = Instant.now().toString();
        CompleteAnchorOnboardingStepRequest data =
                request == null ? CompleteAnchorOnboardingStepRequest.empty() : request;
        switch (step) {
            case PITCH_AND_PROPOSAL -> requireText(data.reference(),
                    "Record the customer proposal appreciation reference before proceeding.");
            case DOCUMENT_COLLECTION -> recordInitialDocuments(onboarding, data.documents(), now);
            case CREDIT_PAPER -> recordRequiredDocument(onboarding, data.documents(), "CREDIT_PAPER", now);
            case ECOSYSTEM_REVIEW -> {
                requireText(data.reference(), "Ecosystem management review reference is required.");
                requireText(data.comments(), "Ecosystem management review comments are required.");
                onboarding.setEcosystemReviewComments(data.comments().trim());
                String outcome = data.ecosystemOutcome() == null
                        ? "" : data.ecosystemOutcome().trim().toUpperCase();
                if (!"APPROVED".equals(outcome) && !"REWORK_REQUIRED".equals(outcome)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Ecosystem review outcome must be APPROVED or REWORK_REQUIRED.");
                }
                if ("REWORK_REQUIRED".equals(outcome)) {
                    onboarding.addMilestone(new AnchorOnboardingMilestone("ECOSYSTEM_REVIEW_RETURNED",
                            actor, data.reference().trim(), data.comments().trim(), now));
                    onboarding.setWorkflowStatus("REWORK_REQUIRED");
                    onboarding.setUpdatedAt(now);
                    return onboardings.save(onboarding);
                }
            }
            case CR_APPROVALS -> {
                if (!recordApprovals(onboarding, "CR", CR_APPROVERS, data.approvals(), now)) {
                    onboarding.setWorkflowStatus("REWORK_REQUIRED");
                    onboarding.setUpdatedAt(now);
                    return onboardings.save(onboarding);
                }
            }
            case MOU_EXECUTION -> {
                String outcome = data.mouOutcome() == null ? "" : data.mouOutcome().trim().toUpperCase();
                if ("RESCHEDULED".equals(outcome)) {
                    recordRequiredDocument(onboarding, data.documents(), "MOU_DRAFT", now);
                    requireText(data.comments(), "Legal revision comments are required.");
                    onboarding.addMilestone(new AnchorOnboardingMilestone("MOU_EXECUTION_LEGAL_RESCHEDULED",
                            actor, valueOrEmpty(data.reference()), data.comments().trim(), now));
                    onboarding.setWorkflowStatus("REWORK_REQUIRED");
                    onboarding.setUpdatedAt(now);
                    return onboardings.save(onboarding);
                }
                if (!"SIGNED".equals(outcome)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "MOU outcome must be SIGNED or RESCHEDULED.");
                }
                recordRequiredDocument(onboarding, data.documents(), "SIGNED_MOU", now);
            }
            case CAR_APPROVALS -> {
                recordRequiredDocument(onboarding, data.documents(), "CAR", now);
                if (!recordApprovals(onboarding, "CAR", CAR_APPROVERS, data.approvals(), now)) {
                    onboarding.setWorkflowStatus("REWORK_REQUIRED");
                    onboarding.setUpdatedAt(now);
                    return onboardings.save(onboarding);
                }
            }
            case APPROVAL_PACK -> recordRequiredDocument(onboarding, data.documents(), "COMPILED_APPROVAL_PACK", now);
            case ANCHOR_FORM -> recordAnchorForm(onboarding, data.anchorForm());
            case IBPS_CREATION -> recordIbpsCreation(onboarding, data.reference());
            case RETAILER_CONSENTS -> recordConsents(onboarding, data.consents(), now);
        }

        onboarding.addMilestone(new AnchorOnboardingMilestone(step.name(), actor,
                valueOrEmpty(data.reference()), valueOrEmpty(data.comments()), now));
        int nextIndex = step.ordinal() + 1;
        if (nextIndex == AnchorOnboardingStep.values().length) {
            onboarding.setWorkflowStatus("READY_FOR_SCORING");
            onboarding.setCurrentStep(null);
        } else {
            onboarding.setCurrentStep(AnchorOnboardingStep.values()[nextIndex]);
        }
        onboarding.setUpdatedAt(now);
        return onboardings.save(onboarding);
    }

    private static void recordInitialDocuments(AnchorOnboarding onboarding,
                                               List<DocumentInput> documents, String now) {
        List<DocumentInput> valid = validateDocuments(documents);
        Set<String> provided = valid.stream().map(document -> document.documentType().trim().toUpperCase())
                .collect(Collectors.toSet());
        if (!provided.containsAll(INITIAL_DOCUMENTS)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Record 3-year audited financials, debtor/creditor ageing, director KYC, and company registration documents.");
        }
        valid.forEach(document -> addDocument(onboarding, document, now));
    }

    private static void recordRequiredDocument(AnchorOnboarding onboarding, List<DocumentInput> documents,
                                               String requiredType, String now) {
        List<DocumentInput> valid = validateDocuments(documents);
        DocumentInput required = valid.stream()
                .filter(document -> requiredType.equals(document.documentType().trim().toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        requiredType + " document reference is required."));
        addDocument(onboarding, required, now);
    }

    private static List<DocumentInput> validateDocuments(List<DocumentInput> documents) {
        if (documents == null || documents.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document metadata is required.");
        }
        for (DocumentInput document : documents) {
            if (document == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document metadata is invalid.");
            }
            requireText(document.documentType(), "Document type is required.");
            requireText(document.fileName(), "Document file name is required.");
            requireText(document.reference(), "Secure document-store reference is required.");
        }
        return documents;
    }

    private static void addDocument(AnchorOnboarding onboarding, DocumentInput document, String now) {
        onboarding.addDocument(new AnchorOnboardingDocument(document.documentType().trim().toUpperCase(),
                document.fileName().trim(), document.reference().trim(), now));
    }

    private static boolean recordApprovals(AnchorOnboarding onboarding, String stage, List<String> requiredRoles,
                                           List<ApprovalInput> approvals, String now) {
        if (approvals == null || approvals.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, stage + " approval trail is required.");
        }
        List<String> received = approvals.stream()
                .filter(approval -> approval != null && approval.approverRole() != null)
                .map(approval -> approval.approverRole().trim().toUpperCase())
                .toList();
        if (!received.equals(requiredRoles)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The ordered " + stage + " approval trail is required: " + String.join(", ", requiredRoles));
        }
        boolean approved = true;
        for (ApprovalInput approval : approvals) {
            if (approval == null || approval.approverRole() == null
                    || !requiredRoles.contains(approval.approverRole().trim().toUpperCase())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Approval role does not match the required " + stage + " approval trail.");
            }
            requireText(approval.approverName(), "Approver name is required.");
            requireDate(approval.signedDate(), "Approval signature date is required.");
            requireText(approval.externalReference(), "Docu-Approval reference is required.");
            String decision = approval.decision() == null ? "" : approval.decision().trim().toUpperCase();
            if (!"APPROVED".equals(decision) && !"REJECTED".equals(decision)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Approval decision must be APPROVED or REJECTED.");
            }
            if ("REJECTED".equals(decision)) {
                requireText(approval.comments(), "Comments are required for a rejected approval.");
            }
            approved = approved && "APPROVED".equals(decision);
            onboarding.addApproval(new AnchorOnboardingApproval(stage,
                    approval.approverRole().trim().toUpperCase(), approval.approverName().trim(),
                    approval.signedDate().trim(),
                    decision, approval.externalReference().trim(), valueOrEmpty(approval.comments()), now));
        }
        return approved;
    }

    private static void recordAnchorForm(AnchorOnboarding onboarding, AnchorFormInput form) {
        if (form == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Anchor creation form details are required.");
        }
        requireText(form.accountName(), "Anchor account name is required.");
        requireText(form.accountNumber(), "Anchor account number is required.");
        requireText(form.collectionAccountNumber(), "Anchor collection account is required.");
        requireText(form.disbursementAccountNumber(), "Anchor disbursement account is required.");
        if (form.partnerTenureMonths() <= 0 || form.facilityLimit() <= 0 || form.baseRate() < 0
                || form.margin() < 0 || form.facilityFeePct() < 0 || form.agreedRate() < 0
                || form.allowedTenors() == null || form.allowedTenors().isEmpty()
                || form.allowedTenors().stream().anyMatch(tenor -> tenor == null || tenor <= 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tenure, positive facility limit, non-negative pricing, and allowed tenors are required.");
        }
        if (Math.abs(form.agreedRate() - (form.baseRate() + form.margin())) > 0.01) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Agreed rate must equal base rate plus margin.");
        }
        onboarding.setAccountName(form.accountName().trim());
        onboarding.setAccountNumber(form.accountNumber().trim());
        onboarding.setCollectionAccountNumber(form.collectionAccountNumber().trim());
        onboarding.setDisbursementAccountNumber(form.disbursementAccountNumber().trim());
        onboarding.setPartnerTenureMonths(form.partnerTenureMonths());
        onboarding.setBaseRate(form.baseRate());
        onboarding.setMargin(form.margin());
        onboarding.setAgreedRate(form.agreedRate());
        onboarding.setFacilityFeePct(form.facilityFeePct());
        onboarding.setFacilityLimit(form.facilityLimit());
        onboarding.setAllowedTenorsCsv(form.allowedTenors().stream().distinct().sorted()
                .map(String::valueOf).collect(Collectors.joining(",")));
    }

    private void recordIbpsCreation(AnchorOnboarding onboarding, String reference) {
        requireText(reference, "IBPS anchor-creation reference is required.");
        if (anchors.existsById(onboarding.getAnchorCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Anchor record already exists.");
        }
        Anchor anchor = new Anchor();
        anchor.setId(onboarding.getAnchorCode());
        anchor.setName(onboarding.getCompanyName());
        anchor.setLogoInitial(onboarding.getCompanyName().substring(0, 1).toUpperCase());
        anchor.setType("ANCHOR");
        anchor.setApprovedLimit(onboarding.getFacilityLimit());
        anchor.setAvailableLimit(onboarding.getFacilityLimit());
        anchor.setCrbStatus("CLEAR");
        anchor.setDrawdownAllowed(false);
        anchor.setCollectionAccountNumber(onboarding.getCollectionAccountNumber());
        anchor.setDisbursementAccountNumber(onboarding.getDisbursementAccountNumber());
        anchor.setFacilityFeePct(onboarding.getFacilityFeePct());
        anchor.setScorecardVersion("PENDING_SCORING");
        anchor.setContactPerson(onboarding.getContactName());
        anchor.setContactPhone(onboarding.getContactPhone());
        anchor.setContactEmail(onboarding.getContactEmail());
        anchor.setAllowedTenors(Arrays.stream(onboarding.getAllowedTenorsCsv().split(","))
                .map(Integer::parseInt).toList());
        anchors.save(anchor);
        onboarding.setIbpsReference(reference.trim());
    }

    private static void recordConsents(AnchorOnboarding onboarding, List<ConsentInput> consents, String now) {
        if (consents == null || consents.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "At least one retailer consent reference is required before scoring.");
        }
        Set<String> retailerIds = new java.util.HashSet<>();
        for (ConsentInput consent : consents) {
            if (consent == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Retailer consent record is invalid.");
            }
            requireText(consent.retailerId(), "Retailer identifier is required.");
            requireText(consent.consentReference(), "Retailer consent reference is required.");
            if (!retailerIds.add(consent.retailerId().trim())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate retailer consent record.");
            }
            onboarding.addRetailerConsent(new AnchorRetailerConsent(
                    consent.retailerId().trim(), consent.consentReference().trim(), now));
        }
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private static void requireDate(String value, String message) {
        requireText(value, message);
        try {
            LocalDate.parse(value.trim());
        } catch (DateTimeParseException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message, exception);
        }
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

}
