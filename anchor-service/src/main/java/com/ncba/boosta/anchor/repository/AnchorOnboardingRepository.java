package com.ncba.boosta.anchor.repository;

import com.ncba.boosta.anchor.models.AnchorOnboarding;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface AnchorOnboardingRepository extends JpaRepository<AnchorOnboarding, String> {
    boolean existsByAnchorCodeIgnoreCase(String anchorCode);
    List<AnchorOnboarding> findAllByCreatedById(String createdById);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from AnchorOnboarding o where o.id = :id")
    Optional<AnchorOnboarding> findLockedById(@Param("id") String id);
}
