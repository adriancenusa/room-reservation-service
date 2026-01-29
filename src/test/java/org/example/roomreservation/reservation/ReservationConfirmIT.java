package org.example.roomreservation.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.example.roomreservation.api.model.ConfirmReservationRequest;
import org.example.roomreservation.api.model.PaymentMode;
import org.example.roomreservation.api.model.ReservationStatus;
import org.example.roomreservation.api.model.RoomSegment;
import org.example.roomreservation.reservation.persistance.ReservationEntity;
import org.example.roomreservation.reservation.persistance.ReservationRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class ReservationConfirmIT {

    private static final String ENDPOINT = "/reservations/confirm";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("reservationdb")
            .withUsername("reservation")
            .withPassword("reservation");

    static final WireMockServer WIREMOCK = new WireMockServer();

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);

        registry.add("clients.credit-card.base-url",
                () -> "http://localhost:" + WIREMOCK.port() + "/host/credit-card-payment-api");
    }

    @BeforeAll
    static void beforeAll() {
        WIREMOCK.start();
    }

    @AfterAll
    static void afterAll() {
        WIREMOCK.stop();
    }

    @BeforeEach
    void beforeEach() {
        reservationRepository.deleteAll();
        WIREMOCK.resetAll();
    }

    @Autowired MockMvc mockMvc;
    @Autowired ReservationRepository reservationRepository;
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void cash_shouldCreateConfirmedReservation() throws Exception {
        ConfirmReservationRequest req = baseRequest(PaymentMode.CASH);
        req.setPaymentReference(null);

        String responseBody = mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.reservationId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String reservationId = objectMapper.readTree(responseBody).get("reservationId").asText();

        Optional<ReservationEntity> saved = reservationRepository.findByReservationId(reservationId);
        assertThat(saved).isPresent();
        assertThat(saved.get().getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(saved.get().getPaymentMode()).isEqualTo(PaymentMode.CASH);
    }

    @Test
    void bankTransfer_shouldCreatePendingPaymentReservation() throws Exception {
        ConfirmReservationRequest req = baseRequest(PaymentMode.BANK_TRANSFER);
        req.setPaymentReference("BT-REF-1");

        String responseBody = mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String reservationId = objectMapper.readTree(responseBody).get("reservationId").asText();

        ReservationEntity saved = reservationRepository.findByReservationId(reservationId).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
        assertThat(saved.getPaymentMode()).isEqualTo(PaymentMode.BANK_TRANSFER);
    }

    @Test
    void creditCardConfirmed_shouldCreateConfirmedReservation() throws Exception {
        stubCreditCardStatus("DL123456", "CONFIRMED");

        ConfirmReservationRequest req = baseRequest(PaymentMode.CREDIT_CARD);
        req.setPaymentReference("DL123456");

        String responseBody = mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String reservationId = objectMapper.readTree(responseBody).get("reservationId").asText();

        ReservationEntity saved = reservationRepository.findByReservationId(reservationId).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(saved.getPaymentMode()).isEqualTo(PaymentMode.CREDIT_CARD);
        assertThat(saved.getPaymentReference()).isEqualTo("DL123456");


        WIREMOCK.verify(1, postRequestedFor(urlEqualTo("/host/credit-card-payment-api/payment-status")));
    }

    @Test
    void creditCardRejected_shouldReturn409_andNotPersist() throws Exception {
        stubCreditCardStatus("DL000000000", "REJECTED");

        ConfirmReservationRequest req = baseRequest(PaymentMode.CREDIT_CARD);
        req.setPaymentReference("DL000000000");

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());

        assertThat(reservationRepository.count()).isEqualTo(0L);
    }

    private ConfirmReservationRequest baseRequest(PaymentMode mode) {
        ConfirmReservationRequest req = new ConfirmReservationRequest();
        req.setCustomerName("John Doe");
        req.setRoomNumber(1207);
        req.setStartDate(LocalDate.of(2026, 3, 10));
        req.setEndDate(LocalDate.of(2026, 3, 12));
        req.setRoomSegment(RoomSegment.MEDIUM);
        req.setPaymentMode(mode);
        req.setPaymentReference("REF-DEFAULT");
        return req;
    }

    private void stubCreditCardStatus(String paymentReference, String status) {
        WIREMOCK.stubFor(WireMock.post(urlPathEqualTo("/host/credit-card-payment-api/payment-status"))
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(matchingJsonPath("$.paymentReference", equalTo(paymentReference)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                {
                  "lastUpdateDate": "2017-07-21T17:32:28Z",
                  "status": "%s"
                }
                """.formatted(status))));
    }
}
