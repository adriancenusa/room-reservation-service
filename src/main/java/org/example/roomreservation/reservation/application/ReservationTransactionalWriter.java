package org.example.roomreservation.reservation.application;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.roomreservation.api.model.ConfirmReservationRequest;
import org.example.roomreservation.api.model.ReservationStatus;
import org.example.roomreservation.reservation.RoomReservationException;
import org.example.roomreservation.reservation.persistance.ReservationEntity;
import org.example.roomreservation.reservation.persistance.ReservationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationTransactionalWriter {
    private final ReservationIdGenerator reservationIdGenerator;
    private final ReservationRepository reservationRepository;

    @Transactional
    public ReservationEntity persistReservation(ConfirmReservationRequest reservationRequest, ReservationStatus reservationStatus) {
        OffsetDateTime now = OffsetDateTime.now();
        var reservationEntity = ReservationEntity.builder()
                .customerName(reservationRequest.getCustomerName())
                .roomNumber(reservationRequest.getRoomNumber())
                .startDate(reservationRequest.getStartDate())
                .endDate(reservationRequest.getEndDate())
                .roomSegment(reservationRequest.getRoomSegment())
                .paymentMode(reservationRequest.getPaymentMode())
                .paymentReference(reservationRequest.getPaymentReference())
                .status(reservationStatus)
                .reservationId(reservationIdGenerator.nextId())
                .createdAt(now)
                .updatedAt(now)
                .build();
        try {
            log.info("Saving reservation: {}", reservationEntity);
            return reservationRepository.saveAndFlush(reservationEntity);
        } catch (DataIntegrityViolationException e) {
            throw new RoomReservationException(
                    "RESERVATION_CONFLICT",
                    HttpStatus.CONFLICT,
                    "Reservation could not be created (conflict). Please retry.");
        }
    }

}
