package org.example.roomreservation.reservation.application.validators;

import org.example.roomreservation.api.model.ConfirmReservationRequest;

public interface ReservationRequestValidator {
    void validate(ConfirmReservationRequest reservationRequest);
}
