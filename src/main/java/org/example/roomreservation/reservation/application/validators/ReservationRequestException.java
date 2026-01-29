package org.example.roomreservation.reservation.application.validators;

import org.example.roomreservation.reservation.RoomReservationException;
import org.springframework.http.HttpStatus;

public class ReservationRequestException extends RoomReservationException {

    public ReservationRequestException(String code, String message) {
        super(code, HttpStatus.BAD_REQUEST, message);
    }
}