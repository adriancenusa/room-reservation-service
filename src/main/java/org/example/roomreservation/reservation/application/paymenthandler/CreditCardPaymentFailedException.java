package org.example.roomreservation.reservation.application.paymenthandler;

import org.example.roomreservation.reservation.RoomReservationException;
import org.springframework.http.HttpStatus;

public class CreditCardPaymentFailedException extends RoomReservationException {
    public CreditCardPaymentFailedException(String code,  String message) {
        super(code, HttpStatus.CONFLICT, message);
    }
}
