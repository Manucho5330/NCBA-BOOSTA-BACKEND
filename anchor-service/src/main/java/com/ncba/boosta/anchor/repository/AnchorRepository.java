package com.ncba.boosta.anchor.repository;

import com.ncba.boosta.anchor.models.Anchor;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnchorRepository extends JpaRepository<Anchor, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Anchor a where a.id = :id")
    java.util.Optional<Anchor> findLockedById(@Param("id") String id);
}
