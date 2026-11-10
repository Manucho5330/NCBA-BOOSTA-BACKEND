package com.ncba.boosta.anchor.repository;

import com.ncba.boosta.anchor.models.LimitReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LimitReservationRepository extends JpaRepository<LimitReservation, String> {
}
