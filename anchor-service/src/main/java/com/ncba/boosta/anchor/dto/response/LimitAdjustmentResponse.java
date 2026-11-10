package com.ncba.boosta.anchor.dto.response;

import com.ncba.boosta.anchor.models.LimitAdjustment;

public record LimitAdjustmentResponse(String anchorId, long amount) {
    public static LimitAdjustmentResponse from(LimitAdjustment adjustment) {
        return new LimitAdjustmentResponse(adjustment.getAnchorId(), adjustment.getAmount());
    }
}
