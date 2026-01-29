package org.example.roomreservation.reservation.application.paymenthandler;

import org.example.roomreservation.api.model.ConfirmReservationRequest;
import org.example.roomreservation.api.model.ReservationStatus;
import org.springframework.stereotype.Component;

@Component
public class CashPaymentModeHandler implements PaymentModeHandler {
    @Override
    public ReservationStatus decideStatus(ConfirmReservationRequest reservationRequest) {
        return ReservationStatus.CONFIRMED;
    }
}
