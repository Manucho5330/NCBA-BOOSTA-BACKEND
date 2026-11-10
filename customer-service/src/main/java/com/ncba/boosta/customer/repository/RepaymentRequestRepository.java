package com.ncba.boosta.customer.repository;

import com.ncba.boosta.customer.models.RepaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepaymentRequestRepository extends JpaRepository<RepaymentRequest, String> {
    List<RepaymentRequest> findByStatusIn(List<String> statuses);
    List<RepaymentRequest> findByLoanIdAndStatusIn(String loanId, List<String> statuses);
    java.util.Optional<RepaymentRequest> findByIdempotencyKey(String idempotencyKey);
}
