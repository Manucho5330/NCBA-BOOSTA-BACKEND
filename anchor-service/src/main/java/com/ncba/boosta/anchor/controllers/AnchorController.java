package com.ncba.boosta.anchor.controllers;

import com.ncba.boosta.anchor.models.Anchor;
import com.ncba.boosta.anchor.models.LimitReservation;
import com.ncba.boosta.anchor.models.LimitAdjustment;
import com.ncba.boosta.anchor.dto.request.CreateLimitAdjustmentRequest;
import com.ncba.boosta.anchor.dto.request.CreateLimitReservationRequest;
import com.ncba.boosta.anchor.dto.request.LimitUpdateRequest;
import com.ncba.boosta.anchor.dto.response.AnchorResponse;
import com.ncba.boosta.anchor.dto.response.DisbursementDetailsResponse;
import com.ncba.boosta.anchor.dto.response.LimitAdjustmentResponse;
import com.ncba.boosta.anchor.dto.response.LimitReservationResponse;
import com.ncba.boosta.anchor.repository.AnchorRepository;
import com.ncba.boosta.anchor.repository.LimitReservationRepository;
import com.ncba.boosta.anchor.repository.LimitAdjustmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/v1/anchors")
public class AnchorController {
    private final AnchorRepository anchors;
    private final LimitReservationRepository reservations;
    private final LimitAdjustmentRepository adjustments;

    public AnchorController(AnchorRepository anchors, LimitReservationRepository reservations,
                            LimitAdjustmentRepository adjustments) {
        this.anchors = anchors;
        this.reservations = reservations;
        this.adjustments = adjustments;
    }

    @GetMapping
    public List<AnchorResponse> list() {
        return anchors.findAll().stream().map(AnchorResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AnchorResponse get(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        if ("ANCHOR".equals(jwt.getClaimAsString("role"))
                && !id.equals(jwt.getClaimAsString("subjectId"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Anchors may only read their own profile.");
        }
        return anchors.findById(id)
                .map(AnchorResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anchor not found."));
    }

    @PutMapping("/{id}/approved-limit")
    @Transactional
    public AnchorResponse amendApprovedLimit(@PathVariable String id, @RequestBody LimitUpdateRequest update) {
        if (update == null || update.approvedLimit() <= 0 || update.availableLimit() < 0
                || update.availableLimit() > update.approvedLimit()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Anchor limit values are invalid.");
        }
        Anchor anchor = anchors.findLockedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anchor not found."));
        anchor.setApprovedLimit(update.approvedLimit());
        anchor.setAvailableLimit(update.availableLimit());
        return AnchorResponse.from(anchors.save(anchor));
    }

    @GetMapping("/{id}/disbursement-details")
    public DisbursementDetailsResponse disbursementDetails(@PathVariable String id) {
        Anchor anchor = anchors.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anchor not found."));
        return new DisbursementDetailsResponse(anchor.getId(), anchor.isDrawdownAllowed(),
                anchor.getDisbursementAccountNumber(), anchor.getCollectionAccountNumber(),
                anchor.getAllowedTenors());
    }

    @PostMapping("/{id}/reservations")
    @Transactional
    public LimitReservationResponse reserve(@PathVariable String id, @RequestBody CreateLimitReservationRequest request) {
        if (request.reservationId() == null || request.reservationId().isBlank() || request.amount() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation id and positive amount are required.");
        }
        LimitReservation existing = reservations.findById(request.reservationId()).orElse(null);
        if (existing != null) {
            if (!id.equals(existing.getAnchorId()) || request.amount() != existing.getAmount()
                    || !"RESERVED".equals(existing.getStatus())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Reservation id is already in use.");
            }
            return LimitReservationResponse.from(existing);
        }
        Anchor anchor = anchors.findLockedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anchor not found."));
        if (!anchor.isDrawdownAllowed()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Drawdowns are disabled for this anchor.");
        }
        if (request.amount() > anchor.getAvailableLimit()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Anchor available limit is insufficient.");
        }
        anchor.setAvailableLimit(anchor.getAvailableLimit() - request.amount());
        anchors.save(anchor);
        return LimitReservationResponse.from(
                reservations.save(new LimitReservation(request.reservationId(), id, request.amount(), "RESERVED")));
    }

    @PostMapping("/{id}/reservations/{reservationId}/release")
    @Transactional
    public LimitReservationResponse release(@PathVariable String id, @PathVariable String reservationId) {
        LimitReservation reservation = reservations.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found."));
        if (!id.equals(reservation.getAnchorId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found.");
        }
        if ("RELEASED".equals(reservation.getStatus())) return LimitReservationResponse.from(reservation);
        Anchor anchor = anchors.findLockedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anchor not found."));
        anchor.setAvailableLimit(Math.min(anchor.getApprovedLimit(),
                anchor.getAvailableLimit() + reservation.getAmount()));
        anchors.save(anchor);
        reservation.setStatus("RELEASED");
        return LimitReservationResponse.from(reservations.save(reservation));
    }

    @PostMapping("/{id}/limit-adjustments")
    @Transactional
    public LimitAdjustmentResponse adjustLimit(
            @PathVariable String id, @RequestBody CreateLimitAdjustmentRequest request) {
        if (request.adjustmentId() == null || request.adjustmentId().isBlank() || request.amount() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adjustment id and positive amount are required.");
        }
        LimitAdjustment existing = adjustments.findById(request.adjustmentId()).orElse(null);
        if (existing != null) {
            if (!id.equals(existing.getAnchorId()) || request.amount() != existing.getAmount()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Adjustment id is already in use.");
            }
            return LimitAdjustmentResponse.from(existing);
        }
        Anchor anchor = anchors.findLockedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anchor not found."));
        anchor.setAvailableLimit(Math.min(anchor.getApprovedLimit(),
                anchor.getAvailableLimit() + request.amount()));
        anchors.save(anchor);
        return LimitAdjustmentResponse.from(
                adjustments.save(new LimitAdjustment(request.adjustmentId(), id, request.amount())));
    }
}
