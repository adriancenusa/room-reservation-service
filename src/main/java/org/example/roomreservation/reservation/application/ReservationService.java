package org.example.roomreservation.reservation.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.roomreservation.api.model.ConfirmReservationRequest;
import org.example.roomreservation.api.model.ConfirmReservationResponse;
import org.example.roomreservation.api.model.ReservationStatus;
import org.example.roomreservation.reservation.application.paymenthandler.PaymentModeHandler;
import org.example.roomreservation.reservation.application.paymenthandler.PaymentModeRegistry;
import org.example.roomreservation.reservation.application.validators.ReservationRequestValidator;
import org.example.roomreservation.reservation.persistance.ReservationEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ReservationService {

    private final List<ReservationRequestValidator> validators;
    private final PaymentModeRegistry paymentModeRegistry;
    private final ReservationTransactionalWriter reservationTransactionalWriter;

    public ConfirmReservationResponse confirmReservation(ConfirmReservationRequest reservationRequest) {
        validators.forEach(v -> v.validate(reservationRequest));
        PaymentModeHandler paymentModeHandler = paymentModeRegistry.getHandler(reservationRequest.getPaymentMode());
        ReservationStatus reservationStatus = paymentModeHandler.decideStatus(reservationRequest);

        ReservationEntity saved = reservationTransactionalWriter.persistReservation(reservationRequest, reservationStatus);
        return new ConfirmReservationResponse(saved.getReservationId(), saved.getStatus());

    }


}
