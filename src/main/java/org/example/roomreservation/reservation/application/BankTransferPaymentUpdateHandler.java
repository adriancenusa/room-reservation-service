package org.example.roomreservation.reservation.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.roomreservation.api.model.PaymentMode;
import org.example.roomreservation.api.model.ReservationStatus;
import org.example.roomreservation.reservation.infra.banktransfer.BankTransferPaymentUpdateEvent;
import org.example.roomreservation.reservation.persistance.ProcessedPaymentEventEntity;
import org.example.roomreservation.reservation.persistance.ProcessedPaymentEventRepository;
import org.example.roomreservation.reservation.persistance.ReservationEntity;
import org.example.roomreservation.reservation.persistance.ReservationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankTransferPaymentUpdateHandler {

    private final ReservationRepository reservationRepository;
    private final ProcessedPaymentEventRepository processedPaymentEventRepository;

    @Transactional
    public void handle(BankTransferPaymentUpdateEvent event) {
        if (eventAlreadyProcessed(event)) {
            return;
        }
        var reservationId = event.getTransactionDescription().substring(11);
        Optional<ReservationEntity> reservation = reservationRepository.findByReservationId(reservationId);
        reservation.ifPresentOrElse(
                (res) -> updateReservation(event, res, reservationId),
                () -> log.warn("Reservation not found for payment update event. reservationId={}, paymentId={}", reservationId, event.getPaymentId()));
    }

    private void updateReservation(BankTransferPaymentUpdateEvent event, ReservationEntity reservation, String reservationId) {
        if (reservation.getPaymentMode() != PaymentMode.BANK_TRANSFER) {
            log.warn("Payment event for non-bank-transfer reservation ignored. paymentId={}, reservationId={}, mode={}",
                    event.getPaymentId(), reservationId, reservation.getPaymentMode());
            return;
        }
        if (reservation.getStatus() == ReservationStatus.PENDING_PAYMENT) {
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservation.setUpdatedAt(OffsetDateTime.now());
            reservationRepository.save(reservation);
        }
    }

    private boolean eventAlreadyProcessed(BankTransferPaymentUpdateEvent event) {
        try {
            processedPaymentEventRepository.saveAndFlush(new ProcessedPaymentEventEntity(event.getPaymentId(), OffsetDateTime.now()));
        } catch (DataIntegrityViolationException duplicate) {
            log.debug("Duplicate payment event ignored. paymentId={}", event.getPaymentId());
            return true;
        }
        return false;
    }
}
