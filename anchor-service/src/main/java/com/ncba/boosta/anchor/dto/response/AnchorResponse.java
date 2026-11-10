package com.ncba.boosta.anchor.dto.response;

import com.ncba.boosta.anchor.models.Anchor;
import java.util.List;

public record AnchorResponse(String id, String name, String logoInitial, String type,
                             long approvedLimit, long availableLimit, String crbStatus,
                             boolean drawdownAllowed, String scorecardVersion, double facilityFeePct,
                             String category, String tier, Long annualTurnoverKes, String contactPerson,
                             String contactPhone, String contactEmail, List<Integer> allowedTenors) {
    public static AnchorResponse from(Anchor anchor) {
        return new AnchorResponse(anchor.getId(), anchor.getName(), anchor.getLogoInitial(), anchor.getType(),
                anchor.getApprovedLimit(), anchor.getAvailableLimit(), anchor.getCrbStatus(),
                anchor.isDrawdownAllowed(), anchor.getScorecardVersion(), anchor.getFacilityFeePct(),
                anchor.getCategory(), anchor.getTier(), anchor.getAnnualTurnoverKes(),
                anchor.getContactPerson(), anchor.getContactPhone(), anchor.getContactEmail(),
                anchor.getAllowedTenors());
    }
}
