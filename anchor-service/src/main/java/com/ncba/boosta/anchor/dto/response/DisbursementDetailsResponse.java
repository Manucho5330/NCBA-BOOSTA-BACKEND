package com.ncba.boosta.anchor.dto.response;

import java.util.List;

public record DisbursementDetailsResponse(String id, boolean drawdownAllowed,
                                          String disbursementAccountNumber,
                                          String collectionAccountNumber,
                                          List<Integer> allowedTenors) {
}
