package org.example.roomreservation.reservation.infra.creditcardpaymentclient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class PaymentStatusResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime lastUpdateDate;

    private CreditCardPaymentStatus status;

}
