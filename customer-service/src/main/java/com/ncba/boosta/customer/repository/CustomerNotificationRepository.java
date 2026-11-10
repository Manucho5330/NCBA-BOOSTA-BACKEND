package com.ncba.boosta.customer.repository;

import com.ncba.boosta.customer.models.CustomerNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerNotificationRepository extends JpaRepository<CustomerNotification, String> {
    List<CustomerNotification> findByOwnerIdOrderByCreatedAtDesc(String ownerId);
}
