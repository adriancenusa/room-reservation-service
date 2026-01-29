package org.example.roomreservation.reservation.application.paymenthandler;

import org.example.roomreservation.api.model.ConfirmReservationRequest;
import org.example.roomreservation.api.model.ReservationStatus;
import org.example.roomreservation.reservation.infra.creditcardpaymentclient.dto.CreditCardPaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditCardPaymentModeHandlerTest {

    @Mock
    private CreditCardPaymentStatusProvider creditCardPaymentStatusProvider;

    @Test
    void decideStatus_returnsConfirmed_whenPaymentIsConfirmed() {
        var handler = new CreditCardPaymentModeHandler(creditCardPaymentStatusProvider);

        ConfirmReservationRequest request = new ConfirmReservationRequest();
        request.setPaymentReference("ref-123");

        when(creditCardPaymentStatusProvider.getStatus("ref-123"))
                .thenReturn(CreditCardPaymentStatus.CONFIRMED);

        var result = handler.decideStatus(request);

        assertThat(ReservationStatus.CONFIRMED).isEqualTo(result);
        verify(creditCardPaymentStatusProvider).getStatus("ref-123");
    }

    @Test
    void decideStatus_throwsWhenPaymentNotConfirmed() {
        var handler = new CreditCardPaymentModeHandler(creditCardPaymentStatusProvider);

        ConfirmReservationRequest request = new ConfirmReservationRequest();
        request.setPaymentReference("ref-456");

        when(creditCardPaymentStatusProvider.getStatus("ref-456"))
                .thenReturn(null);

        var ex = assertThrows(CreditCardPaymentFailedException.class, () -> handler.decideStatus(request));
        assertThat("Credit card payment failed").isEqualTo(ex.getMessage());
        verify(creditCardPaymentStatusProvider).getStatus("ref-456");
    }
}