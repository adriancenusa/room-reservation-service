package org.example.roomreservation.reservation;

import org.example.roomreservation.api.model.PaymentMode;
import org.example.roomreservation.api.model.ReservationStatus;
import org.example.roomreservation.api.model.RoomSegment;
import org.example.roomreservation.reservation.application.scheduler.BankTransferAutoCancellationJob;
import org.example.roomreservation.reservation.persistance.ReservationEntity;
import org.example.roomreservation.reservation.persistance.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class BankTransferAutoCancellationIT {

    private static final ZoneId HOTEL_ZONE = ZoneId.of("Europe/Amsterdam");

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("reservationdb")
            .withUsername("reservation")
            .withPassword("reservation");

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    }
    @Autowired ReservationRepository reservationRepository;
    @Autowired BankTransferAutoCancellationJob job;

    @BeforeEach
    void clean() {
        reservationRepository.deleteAll();
    }

    @Test
    void shouldCancelPendingBankTransferReservations_twoDaysBeforeStartDate() {
        LocalDate today = LocalDate.now(HOTEL_ZONE);

        ReservationEntity due = ReservationEntity.builder()
                .reservationId("DUE00001")
                .customerName("Alice")
                .roomNumber(1207)
                .startDate(today.plusDays(2))
                .endDate(today.plusDays(4))
                .roomSegment(RoomSegment.MEDIUM)
                .paymentMode(PaymentMode.BANK_TRANSFER)
                .paymentReference("BT-REF-1")
                .status(ReservationStatus.PENDING_PAYMENT)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        ReservationEntity notDue = ReservationEntity.builder()
                .reservationId("DUE00002") // 8 chars
                .customerName("Bob")
                .roomNumber(1208)
                .startDate(today.plusDays(3))
                .endDate(today.plusDays(5))
                .roomSegment(RoomSegment.MEDIUM)
                .paymentMode(PaymentMode.BANK_TRANSFER)
                .paymentReference("BT-REF-2")
                .status(ReservationStatus.PENDING_PAYMENT)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        reservationRepository.saveAndFlush(due);
        reservationRepository.saveAndFlush(notDue);

        job.cancelOverdueBankTransfers();

        ReservationEntity dueAfter = reservationRepository.findByReservationId("DUE00001").orElseThrow();
        ReservationEntity notDueAfter = reservationRepository.findByReservationId("DUE00002").orElseThrow();

        assertThat(dueAfter.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(notDueAfter.getStatus()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
    }
}
