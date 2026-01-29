package org.example.roomreservation.reservation.persistance;

import org.example.roomreservation.api.model.PaymentMode;
import org.example.roomreservation.api.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<ReservationEntity, UUID> {
    Optional<ReservationEntity> findByReservationId(String reservationId);
    List<ReservationEntity> findByPaymentModeAndStatusAndStartDateLessThanEqual(
            PaymentMode paymentMode,
            ReservationStatus status,
            LocalDate startDateCutoff
    );
}