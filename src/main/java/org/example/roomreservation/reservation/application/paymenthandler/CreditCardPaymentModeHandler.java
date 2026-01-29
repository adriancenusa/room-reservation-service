package org.example.roomreservation.reservation.application.paymenthandler;

import lombok.RequiredArgsConstructor;
import org.example.roomreservation.api.model.ConfirmReservationRequest;
import org.example.roomreservation.api.model.ReservationStatus;
import org.example.roomreservation.reservation.infra.creditcardpaymentclient.dto.CreditCardPaymentStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreditCardPaymentModeHandler implements PaymentModeHandler {

    private final CreditCardPaymentStatusProvider creditCardPaymentStatusProvider;

    @Override
    public ReservationStatus decideStatus(ConfirmReservationRequest reservationRequest) {
        var status = creditCardPaymentStatusProvider.getStatus(reservationRequest.getPaymentReference());
        if (status == CreditCardPaymentStatus.CONFIRMED)
            return ReservationStatus.CONFIRMED;
        throw new CreditCardPaymentFailedException("PAYMENT_FAILED", "Credit card payment failed");
    }
}
