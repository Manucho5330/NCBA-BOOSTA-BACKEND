package com.ncba.boosta.anchor.services;

import com.ncba.boosta.anchor.models.AnchorOnboarding;
import com.ncba.boosta.anchor.models.AnchorOnboardingStep;
import com.ncba.boosta.anchor.dto.request.CreateAnchorOnboardingRequest;
import com.ncba.boosta.anchor.dto.request.CompleteAnchorOnboardingStepRequest;
import com.ncba.boosta.anchor.repository.AnchorOnboardingRepository;
import com.ncba.boosta.anchor.repository.AnchorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnchorOnboardingServiceTest {
    @Mock
    private AnchorOnboardingRepository onboardings;
    @Mock
    private AnchorRepository anchors;
    @InjectMocks
    private AnchorOnboardingService service;

    @Test
    void startsAtProposalAndRequiresSequentialGates() {
        when(onboardings.existsByAnchorCodeIgnoreCase("ANCHOR-TEST")).thenReturn(false);
        when(anchors.existsById("ANCHOR-TEST")).thenReturn(false);
        when(onboardings.save(any(AnchorOnboarding.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AnchorOnboarding created = service.create(new CreateAnchorOnboardingRequest(
                "ANCHOR-TEST", "Test Company", "Nairobi", "BM Name", "RM Name",
                "Contact", "+254700000000", "contact@example.com"), "Maker", "maker-id");

        assertEquals(AnchorOnboardingStep.PITCH_AND_PROPOSAL, created.getCurrentStep());
        assertEquals("IN_PROGRESS", created.getWorkflowStatus());

        when(onboardings.findLockedById(created.getId())).thenReturn(Optional.of(created));
        AnchorOnboarding afterPitch = service.completeStep(created.getId(),
                AnchorOnboardingStep.PITCH_AND_PROPOSAL,
                new CompleteAnchorOnboardingStepRequest(
                        "proposal-agreement-001", null, null, null, null, null, null, null),
                "Maker", "maker-id", false);

        assertEquals(AnchorOnboardingStep.DOCUMENT_COLLECTION, afterPitch.getCurrentStep());
        assertEquals(1, afterPitch.getMilestones().size());

        assertThrows(ResponseStatusException.class, () -> service.completeStep(created.getId(),
                AnchorOnboardingStep.CREDIT_PAPER,
                CompleteAnchorOnboardingStepRequest.empty(),
                "Maker", "maker-id", false));
        verify(onboardings, times(2)).save(created);
    }

    @Test
    void ecosystemReviewReworkKeepsCaseAtReviewGate() {
        AnchorOnboarding onboarding = new AnchorOnboarding("case-id", "ANCHOR-TEST", "Test Company",
                "Nairobi", "BM Name", "RM Name", "Contact", "+254700000000",
                "contact@example.com", "Maker", "maker-id", "2025-01-01T00:00:00Z");
        onboarding.setCurrentStep(AnchorOnboardingStep.ECOSYSTEM_REVIEW);
        when(onboardings.findLockedById("case-id")).thenReturn(Optional.of(onboarding));
        when(onboardings.save(any(AnchorOnboarding.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AnchorOnboarding returned = service.completeStep("case-id", AnchorOnboardingStep.ECOSYSTEM_REVIEW,
                new CompleteAnchorOnboardingStepRequest(
                        "review-ref-001", "Clarify the debtor ageing analysis", "REWORK_REQUIRED",
                        null, null, null, null, null),
                "Maker", "maker-id", false);

        assertEquals(AnchorOnboardingStep.ECOSYSTEM_REVIEW, returned.getCurrentStep());
        assertEquals("REWORK_REQUIRED", returned.getWorkflowStatus());
        assertEquals(1, returned.getMilestones().size());
    }
}
