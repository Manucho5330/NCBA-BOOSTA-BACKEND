package com.ncba.boosta.customer.controllers;

import com.ncba.boosta.customer.dto.request.CreateNotificationRequest;
import com.ncba.boosta.customer.dto.request.NotificationReadRequest;
import com.ncba.boosta.customer.dto.response.NotificationResponse;
import com.ncba.boosta.customer.models.CustomerNotification;
import com.ncba.boosta.customer.repository.CustomerNotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final CustomerNotificationRepository notifications;

    public NotificationController(CustomerNotificationRepository notifications) {
        this.notifications = notifications;
    }

    @GetMapping
    public List<NotificationResponse> list(@RequestParam(required = false) String ownerId,
                                           @AuthenticationPrincipal Jwt jwt) {
        String role = jwt.getClaimAsString("role");
        String subjectId = jwt.getClaimAsString("subjectId");
        if ("CUSTOMER".equals(role) || "ANCHOR".equals(role)) {
            ownerId = subjectId;
        }
        List<CustomerNotification> results = ownerId == null || ownerId.isBlank()
                ? notifications.findAll()
                : notifications.findByOwnerIdOrderByCreatedAtDesc(ownerId);
        return results.stream().map(NotificationResponse::from).toList();
    }

    @PostMapping
    public NotificationResponse create(@RequestBody CreateNotificationRequest request) {
        if (request.ownerId() == null || request.ownerId().isBlank()
                || request.type() == null || request.type().isBlank()
                || request.title() == null || request.title().isBlank()
                || request.body() == null || request.body().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Notification owner, type, title and body are required.");
        }
        CustomerNotification notification = new CustomerNotification();
        notification.setId("notification-" + UUID.randomUUID());
        notification.setOwnerId(request.ownerId());
        notification.setType(request.type());
        notification.setTitle(request.title());
        notification.setBody(request.body());
        notification.setCreatedAt(Instant.now().toString());
        notification.setState("UNREAD");
        notification.setLoanId(request.loanId());
        notification.setActionUrl(request.actionUrl());
        notification.setRequestId(request.requestId());
        return NotificationResponse.from(notifications.save(notification));
    }

    @PatchMapping("/{id}")
    public NotificationResponse updateReadState(@PathVariable String id,
                                                @RequestBody NotificationReadRequest request,
                                                @AuthenticationPrincipal Jwt jwt) {
        CustomerNotification notification = notifications.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found."));
        requireOwnerOrStaff(notification, jwt);
        String state = request.state();
        if (state == null && request.read() != null) state = request.read() ? "READ" : "UNREAD";
        if (state == null || !List.of("UNREAD", "READ", "ARCHIVED").contains(state)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Notification state is invalid.");
        }
        notification.setState(state);
        return NotificationResponse.from(notifications.save(notification));
    }

    @PatchMapping("/read-all")
    public List<NotificationResponse> markAllRead(@AuthenticationPrincipal Jwt jwt) {
        String role = jwt.getClaimAsString("role");
        if (!"CUSTOMER".equals(role) && !"ANCHOR".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Mark-all-read is only available to customer and anchor accounts.");
        }
        List<CustomerNotification> owned = notifications.findByOwnerIdOrderByCreatedAtDesc(
                jwt.getClaimAsString("subjectId"));
        owned.forEach(notification -> notification.setState("READ"));
        return notifications.saveAll(owned).stream().map(NotificationResponse::from).toList();
    }

    private static void requireOwnerOrStaff(CustomerNotification notification, Jwt jwt) {
        String role = jwt.getClaimAsString("role");
        if (("CUSTOMER".equals(role) || "ANCHOR".equals(role))
                && !notification.getOwnerId().equals(jwt.getClaimAsString("subjectId"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Portal users may only update their own notifications.");
        }
    }
}
