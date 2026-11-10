package com.ncba.boosta.customer.repository;

import com.ncba.boosta.customer.models.PortfolioTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioTransactionRepository extends JpaRepository<PortfolioTransaction, String> {
    java.util.List<PortfolioTransaction> findByAnchorId(String anchorId);
}
