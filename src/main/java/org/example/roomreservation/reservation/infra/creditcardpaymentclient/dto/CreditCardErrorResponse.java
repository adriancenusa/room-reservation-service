package org.example.roomreservation.reservation.infra.creditcardpaymentclient.dto;

import lombok.Data;

@Data
public class CreditCardErrorResponse {
    private final String error;
    private final String message;
}
