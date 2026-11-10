package com.ncba.boosta.customer.repository;

import com.ncba.boosta.customer.models.DrawdownRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DrawdownRequestRepository extends JpaRepository<DrawdownRequest, String> {
    List<DrawdownRequest> findByStatusIn(List<String> statuses);
    List<DrawdownRequest> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    java.util.Optional<DrawdownRequest> findByIdempotencyKey(String idempotencyKey);
}
