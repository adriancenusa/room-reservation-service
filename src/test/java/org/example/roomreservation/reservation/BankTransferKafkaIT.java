package org.example.roomreservation.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.awaitility.Awaitility;
import org.example.roomreservation.api.model.ConfirmReservationRequest;
import org.example.roomreservation.api.model.PaymentMode;
import org.example.roomreservation.api.model.ReservationStatus;
import org.example.roomreservation.api.model.RoomSegment;
import org.example.roomreservation.reservation.infra.banktransfer.BankTransferPaymentUpdateEvent;
import org.example.roomreservation.reservation.persistance.ReservationEntity;
import org.example.roomreservation.reservation.persistance.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class BankTransferKafkaIT {

    private static final String ENDPOINT = "/reservations/confirm";
    private static final String TOPIC = "bank-transfer-payment-update";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("reservationdb")
            .withUsername("reservation")
            .withPassword("reservation");

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1")
    );

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.kafka.producer.key-serializer",
                () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.producer.value-serializer",
                () -> "org.springframework.kafka.support.serializer.JsonSerializer");
    }

    @Autowired MockMvc mockMvc;
    @Autowired ReservationRepository reservationRepository;
    @Autowired KafkaTemplate<String, BankTransferPaymentUpdateEvent> kafkaTemplate;
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void clean() {
        reservationRepository.deleteAll();
    }

    @Test
    void bankTransferReservation_shouldBecomeConfirmed_afterKafkaPaymentEvent() throws Exception {
        ConfirmReservationRequest req = new ConfirmReservationRequest();
        req.setCustomerName("Jane Doe");
        req.setRoomNumber(1207);
        req.setStartDate(LocalDate.of(2026, 3, 10));
        req.setEndDate(LocalDate.of(2026, 3, 12));
        req.setRoomSegment(RoomSegment.MEDIUM);
        req.setPaymentMode(PaymentMode.BANK_TRANSFER);
        req.setPaymentReference("BT-REF-1");

        String responseBody = mockMvc.perform(post(ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String reservationId = objectMapper.readTree(responseBody).get("reservationId").asText();

        ReservationEntity pending = reservationRepository.findByReservationId(reservationId).orElseThrow();
        assertThat(pending.getStatus()).isEqualTo(ReservationStatus.PENDING_PAYMENT);

        BankTransferPaymentUpdateEvent event = new BankTransferPaymentUpdateEvent(
                "payment-" + System.nanoTime(),
                "NL00BANK0123456789",
                new BigDecimal("100.00"),
                "1401541457 " + reservationId
        );

        kafkaTemplate.send(TOPIC, event.getPaymentId(), event).get(5, TimeUnit.SECONDS);

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> {
                    ReservationEntity updated = reservationRepository.findByReservationId(reservationId).orElseThrow();
                    assertThat(updated.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
                });
    }
}
