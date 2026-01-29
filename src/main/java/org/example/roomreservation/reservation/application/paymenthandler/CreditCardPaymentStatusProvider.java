package org.example.roomreservation.reservation.application.paymenthandler;

import org.example.roomreservation.reservation.infra.creditcardpaymentclient.dto.CreditCardPaymentStatus;
import org.springframework.stereotype.Component;

@Component
public interface CreditCardPaymentStatusProvider {

    CreditCardPaymentStatus getStatus(String paymentReference);
}
