package com.ncba.boosta.auth.repository;

import com.ncba.boosta.auth.models.AuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthAccountRepository extends JpaRepository<AuthAccount, Long> {
    Optional<AuthAccount> findByIdentifier(String identifier);
    boolean existsByIdentifier(String identifier);
    boolean existsByAdminMarker(String adminMarker);
}
