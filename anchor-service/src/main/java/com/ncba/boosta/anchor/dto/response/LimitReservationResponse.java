package com.ncba.boosta.anchor.dto.response;

import com.ncba.boosta.anchor.models.LimitReservation;

public record LimitReservationResponse(String id, String anchorId, long amount, String status) {
    public static LimitReservationResponse from(LimitReservation reservation) {
        return new LimitReservationResponse(reservation.getId(), reservation.getAnchorId(),
                reservation.getAmount(), reservation.getStatus());
    }
}
