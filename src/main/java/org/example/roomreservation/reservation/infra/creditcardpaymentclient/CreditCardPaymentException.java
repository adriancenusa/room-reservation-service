package org.example.roomreservation.reservation.infra.creditcardpaymentclient;

import org.example.roomreservation.reservation.RoomReservationException;
import org.springframework.http.HttpStatus;

public class CreditCardPaymentException extends RoomReservationException {
    public CreditCardPaymentException(String code, String message) {
        super(code, HttpStatus.CONFLICT, message);
    }

    public CreditCardPaymentException(String code, HttpStatus status, String message) {
        super(code, status, message);
    }
}
