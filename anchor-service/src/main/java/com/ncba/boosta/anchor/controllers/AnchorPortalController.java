package com.ncba.boosta.anchor.controllers;

import com.ncba.boosta.anchor.dto.request.UpdateTurnoverReviewRequest;
import com.ncba.boosta.anchor.dto.response.AnchorPortalCustomerResponse;
import com.ncba.boosta.anchor.dto.response.AnchorPortalTransactionResponse;
import com.ncba.boosta.anchor.dto.response.AnchorPortalUserResponse;
import com.ncba.boosta.anchor.dto.response.SalesPointResponse;
import com.ncba.boosta.anchor.dto.response.TurnoverReviewResponse;
import com.ncba.boosta.anchor.services.AnchorPortalService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/anchor-portal/{anchorId}")
public class AnchorPortalController {
    private final AnchorPortalService portal;

    public AnchorPortalController(AnchorPortalService portal) {
        this.portal = portal;
    }

    @GetMapping
    public AnchorPortalUserResponse user(@PathVariable String anchorId, @AuthenticationPrincipal Jwt jwt) {
        authorize(anchorId, jwt);
        return portal.getUser(anchorId);
    }

    @GetMapping("/customers")
    public List<AnchorPortalCustomerResponse> customers(
            @PathVariable String anchorId, @AuthenticationPrincipal Jwt jwt) {
        authorize(anchorId, jwt);
        return portal.getCustomers(anchorId, bearer(jwt));
    }

    @GetMapping("/sales")
    public List<SalesPointResponse> sales(
            @PathVariable String anchorId, @RequestParam String period, @AuthenticationPrincipal Jwt jwt) {
        authorize(anchorId, jwt);
        return portal.getSales(anchorId, period, bearer(jwt));
    }

    @GetMapping("/turnover-review")
    public TurnoverReviewResponse turnoverReview(
            @PathVariable String anchorId, @AuthenticationPrincipal Jwt jwt) {
        authorize(anchorId, jwt);
        return TurnoverReviewResponse.from(portal.getTurnoverReview(anchorId));
    }

    @PutMapping("/turnover-review")
    public TurnoverReviewResponse updateTurnoverReview(
            @PathVariable String anchorId, @RequestBody UpdateTurnoverReviewRequest update,
            @AuthenticationPrincipal Jwt jwt) {
        authorize(anchorId, jwt);
        return TurnoverReviewResponse.from(portal.updateTurnoverReview(anchorId, update));
    }

    @GetMapping("/transactions")
    public List<AnchorPortalTransactionResponse> transactions(
            @PathVariable String anchorId, @AuthenticationPrincipal Jwt jwt) {
        authorize(anchorId, jwt);
        return portal.getTransactions(anchorId, bearer(jwt));
    }

    private static void authorize(String anchorId, Jwt jwt) {
        if (!"ANCHOR".equals(jwt.getClaimAsString("role"))
                || !anchorId.equals(jwt.getClaimAsString("subjectId"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Anchor portal data can only be accessed by its linked account.");
        }
    }

    private static String bearer(Jwt jwt) {
        return "Bearer " + jwt.getTokenValue();
    }
}
