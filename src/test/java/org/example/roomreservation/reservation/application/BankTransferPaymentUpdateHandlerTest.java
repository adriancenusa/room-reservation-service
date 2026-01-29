// language: java
package org.example.roomreservation.reservation.application;

import org.example.roomreservation.api.model.PaymentMode;
import org.example.roomreservation.api.model.ReservationStatus;
import org.example.roomreservation.reservation.infra.banktransfer.BankTransferPaymentUpdateEvent;
import org.example.roomreservation.reservation.persistance.ProcessedPaymentEventEntity;
import org.example.roomreservation.reservation.persistance.ProcessedPaymentEventRepository;
import org.example.roomreservation.reservation.persistance.ReservationEntity;
import org.example.roomreservation.reservation.persistance.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankTransferPaymentUpdateHandlerTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ProcessedPaymentEventRepository processedPaymentEventRepository;

    @Mock
    private BankTransferPaymentUpdateEvent event;

    private BankTransferPaymentUpdateHandler createHandler() {
        return new BankTransferPaymentUpdateHandler(reservationRepository, processedPaymentEventRepository);
    }

    @Test
    void handle_whenEventAlreadyProcessed_doesNotCallReservationRepository() {
        var handler = createHandler();

        when(event.getPaymentId()).thenReturn("payment-1");
        when(processedPaymentEventRepository.saveAndFlush(any(ProcessedPaymentEventEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        handler.handle(event);

        verify(processedPaymentEventRepository, times(1)).saveAndFlush(any(ProcessedPaymentEventEntity.class));
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void handle_whenReservationNotFound_noSaveCalled() {
        var handler = createHandler();

        when(event.getPaymentId()).thenReturn("payment-2");
        when(event.getTransactionDescription()).thenReturn("00000000000res-2");
        when(processedPaymentEventRepository.saveAndFlush(any(ProcessedPaymentEventEntity.class)))
                .thenReturn(new ProcessedPaymentEventEntity("payment-2", OffsetDateTime.now()));
        when(reservationRepository.findByReservationId("res-2")).thenReturn(Optional.empty());

        handler.handle(event);

        verify(processedPaymentEventRepository, times(1)).saveAndFlush(any(ProcessedPaymentEventEntity.class));
        verify(reservationRepository, times(1)).findByReservationId("res-2");
        verify(reservationRepository, never()).save(any(ReservationEntity.class));
    }

    @Test
    void handle_whenNonBankTransferReservation_ignoresPaymentEvent() {
        var handler = createHandler();

        when(event.getPaymentId()).thenReturn("payment-3");
        when(event.getTransactionDescription()).thenReturn("00000000000res-3");
        when(processedPaymentEventRepository.saveAndFlush(any(ProcessedPaymentEventEntity.class)))
                .thenReturn(new ProcessedPaymentEventEntity("payment-3", OffsetDateTime.now()));

        ReservationEntity reservation = ReservationEntity.builder()
                .reservationId("res-3")
                .paymentMode(PaymentMode.CREDIT_CARD)
                .status(ReservationStatus.PENDING_PAYMENT)
                .build();


        when(reservationRepository.findByReservationId("res-3")).thenReturn(Optional.of(reservation));

        handler.handle(event);

        verify(processedPaymentEventRepository, times(1)).saveAndFlush(any(ProcessedPaymentEventEntity.class));
        verify(reservationRepository, times(1)).findByReservationId("res-3");
        verify(reservationRepository, never()).save(any(ReservationEntity.class));
    }

    @Test
    void handle_whenPendingBankTransfer_updatesStatusToConfirmedAndSaves() {
        var handler = createHandler();

        when(event.getPaymentId()).thenReturn("payment-4");
        when(event.getTransactionDescription()).thenReturn("00000000000res-4");
        when(processedPaymentEventRepository.saveAndFlush(any(ProcessedPaymentEventEntity.class)))
                .thenReturn(new ProcessedPaymentEventEntity("payment-4", OffsetDateTime.now()));

        ReservationEntity reservation = ReservationEntity.builder()
                .reservationId("res-4")
                .paymentMode(PaymentMode.BANK_TRANSFER)
                .status(ReservationStatus.PENDING_PAYMENT)
                .build();

        reservation.setUpdatedAt(null);

        when(reservationRepository.findByReservationId("res-4")).thenReturn(Optional.of(reservation));

        OffsetDateTime before = OffsetDateTime.now();
        handler.handle(event);
        OffsetDateTime after = OffsetDateTime.now();

        ArgumentCaptor<ReservationEntity> captor = ArgumentCaptor.forClass(ReservationEntity.class);
        verify(reservationRepository, times(1)).save(captor.capture());
        ReservationEntity saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isBetween(before.minusSeconds(1), after.plusSeconds(1));
    }
}