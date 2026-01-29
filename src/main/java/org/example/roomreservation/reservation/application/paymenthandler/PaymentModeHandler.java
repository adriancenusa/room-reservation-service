package org.example.roomreservation.reservation.application.paymenthandler;

import org.example.roomreservation.api.model.ConfirmReservationRequest;
import org.example.roomreservation.api.model.PaymentMode;
import org.example.roomreservation.api.model.ReservationStatus;

public interface PaymentModeHandler {
    ReservationStatus decideStatus(ConfirmReservationRequest reservationRequest);
}
