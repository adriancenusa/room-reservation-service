package org.example.roomreservation.reservation.infra.creditcardpaymentclient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentStatusRetrievalRequest {
    @NotBlank
    private final String paymentReference;
}
