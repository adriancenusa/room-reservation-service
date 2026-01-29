package org.example.roomreservation.reservation.application;

import org.example.roomreservation.api.model.ConfirmReservationRequest;
import org.example.roomreservation.api.model.PaymentMode;
import org.example.roomreservation.api.model.ReservationStatus;
import org.example.roomreservation.reservation.application.paymenthandler.CreditCardPaymentFailedException;
import org.example.roomreservation.reservation.application.paymenthandler.PaymentModeHandler;
import org.example.roomreservation.reservation.application.paymenthandler.PaymentModeRegistry;
import org.example.roomreservation.reservation.application.validators.ReservationRequestException;
import org.example.roomreservation.reservation.application.validators.ReservationRequestValidator;
import org.example.roomreservation.reservation.persistance.ReservationEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRequestValidator validator;

    @Mock
    private PaymentModeRegistry paymentModeRegistry;

    @Mock
    private PaymentModeHandler paymentModeHandler;

    @Mock
    private ReservationTransactionalWriter reservationTransactionalWriter;

    @Mock
    private ReservationEntity savedEntity;

    @Test
    void confirmReservation_success_runsValidators_callsHandler_persists_andReturnsResponse() {
        ReservationService service = new ReservationService(List.of(validator), paymentModeRegistry, reservationTransactionalWriter);

        ConfirmReservationRequest request = new ConfirmReservationRequest();
        request.setPaymentMode(PaymentMode.CREDIT_CARD);

        when(paymentModeRegistry.getHandler(PaymentMode.CREDIT_CARD)).thenReturn(paymentModeHandler);
        when(paymentModeHandler.decideStatus(request)).thenReturn(ReservationStatus.CONFIRMED);
        when(reservationTransactionalWriter.persistReservation(request, ReservationStatus.CONFIRMED)).thenReturn(savedEntity);
        when(savedEntity.getReservationId()).thenReturn("res-123");
        when(savedEntity.getStatus()).thenReturn(ReservationStatus.CONFIRMED);

        var response = service.confirmReservation(request);

        verify(validator).validate(request);
        verify(paymentModeRegistry).getHandler(PaymentMode.CREDIT_CARD);
        verify(paymentModeHandler).decideStatus(request);
        verify(reservationTransactionalWriter).persistReservation(request, ReservationStatus.CONFIRMED);

        assertThat(response).isNotNull();
        assertThat(response.getReservationId()).isEqualTo("res-123");
        assertThat(response.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void confirmReservation_whenValidatorThrows_propagatesAndDoesNotPersist() {
        ReservationService service = new ReservationService(List.of(validator), paymentModeRegistry, reservationTransactionalWriter);

        ConfirmReservationRequest request = new ConfirmReservationRequest();
        request.setPaymentMode(PaymentMode.CASH);

        doThrow(new ReservationRequestException("invalid", "message")).when(validator).validate(request);

        assertThatThrownBy(() -> service.confirmReservation(request))
                .isInstanceOf(ReservationRequestException.class)
                .hasMessageContaining("message");

        verify(reservationTransactionalWriter, never()).persistReservation(any(), any());
    }

    @Test
    void confirmReservation_whenPaymentHandlerThrows_propagatesAndDoesNotPersist() {
        ReservationService service = new ReservationService(List.of(validator), paymentModeRegistry, reservationTransactionalWriter);

        ConfirmReservationRequest request = new ConfirmReservationRequest();
        request.setPaymentMode(PaymentMode.CREDIT_CARD);

        when(paymentModeRegistry.getHandler(PaymentMode.CREDIT_CARD)).thenReturn(paymentModeHandler);
        when(paymentModeHandler.decideStatus(request)).thenThrow(new CreditCardPaymentFailedException("payment failed", "msg"));

        assertThatThrownBy(() -> service.confirmReservation(request))
                .isInstanceOf(CreditCardPaymentFailedException.class)
                .hasMessageContaining("msg");

        verify(reservationTransactionalWriter, never()).persistReservation(any(), any());
    }
}