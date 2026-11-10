package com.ncba.boosta.anchor.dto.request;

public record CreateAnchorOnboardingRequest(String anchorCode, String companyName, String branchRegion,
                                            String businessManagerName, String relationshipManagerName,
                                            String contactName, String contactPhone, String contactEmail) {
}
