package org.example.roomreservation.reservation.application;

import org.example.roomreservation.api.model.ConfirmReservationRequest;
import org.example.roomreservation.api.model.PaymentMode;
import org.example.roomreservation.api.model.ReservationStatus;
import org.example.roomreservation.api.model.RoomSegment;
import org.example.roomreservation.reservation.RoomReservationException;
import org.example.roomreservation.reservation.application.ReservationIdGenerator;
import org.example.roomreservation.reservation.application.ReservationTransactionalWriter;
import org.example.roomreservation.reservation.persistance.ReservationEntity;
import org.example.roomreservation.reservation.persistance.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationTransactionalWriterTest {

    @Mock
    private ReservationIdGenerator reservationIdGenerator;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationEntity savedEntity;

    @Captor
    private ArgumentCaptor<ReservationEntity> entityCaptor;

    @Test
    void persistReservation_success_persistsEntityAndReturnsSaved() {
        ReservationTransactionalWriter writer = new ReservationTransactionalWriter(reservationIdGenerator, reservationRepository);

        ConfirmReservationRequest request = new ConfirmReservationRequest();
        request.setCustomerName("Alice");
        request.setRoomNumber(101);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(2));
        request.setRoomSegment(RoomSegment.MEDIUM);
        request.setPaymentMode(PaymentMode.CREDIT_CARD);
        request.setPaymentReference("pay-ref-1");

        when(reservationIdGenerator.nextId()).thenReturn("res-123");
        when(reservationRepository.saveAndFlush(any(ReservationEntity.class))).thenReturn(savedEntity);

        ReservationEntity result = writer.persistReservation(request, ReservationStatus.CONFIRMED);

        verify(reservationRepository).saveAndFlush(entityCaptor.capture());
        ReservationEntity captured = entityCaptor.getValue();

        assertThat(captured.getCustomerName()).isEqualTo("Alice");
        assertThat(captured.getRoomNumber()).isEqualTo(101);
        assertThat(captured.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(captured.getEndDate()).isEqualTo(request.getEndDate());
        assertThat(captured.getRoomSegment()).isEqualTo(RoomSegment.MEDIUM);
        assertThat(captured.getPaymentMode()).isEqualTo(PaymentMode.CREDIT_CARD);
        assertThat(captured.getPaymentReference()).isEqualTo("pay-ref-1");
        assertThat(captured.getStatus()).isEqualTo(org.example.roomreservation.api.model.ReservationStatus.CONFIRMED);
        assertThat(captured.getReservationId()).isEqualTo("res-123");
        assertThat(captured.getCreatedAt()).isNotNull();
        assertThat(captured.getUpdatedAt()).isNotNull();
        assertThat(result).isSameAs(savedEntity);
    }

    @Test
    void persistReservation_whenRepositoryThrows_translatesToRoomReservationException() {
        ReservationTransactionalWriter writer = new ReservationTransactionalWriter(reservationIdGenerator, reservationRepository);

        ConfirmReservationRequest request = new ConfirmReservationRequest();
        request.setCustomerName("Bob");
        request.setRoomNumber(202);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(1));
        request.setRoomSegment(RoomSegment.MEDIUM);
        request.setPaymentMode(PaymentMode.CASH);
        request.setPaymentReference("pay-ref-2");

        when(reservationIdGenerator.nextId()).thenReturn("res-456");
        when(reservationRepository.saveAndFlush(any(ReservationEntity.class)))
                .thenThrow(new DataIntegrityViolationException("unique constraint"));

        assertThatThrownBy(() -> writer.persistReservation(request, org.example.roomreservation.api.model.ReservationStatus.CONFIRMED))
                .isInstanceOf(RoomReservationException.class)
                .hasMessageContaining("Reservation could not be created (conflict). Please retry.");
    }
}