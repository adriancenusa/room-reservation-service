package org.example.roomreservation.reservation.api;

import org.example.roomreservation.api.model.ConfirmReservationRequest;
import org.example.roomreservation.api.model.ConfirmReservationResponse;
import org.example.roomreservation.reservation.application.ReservationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @Test
    void confirmReservation_returnsOkAndBody() {
        ReservationController controller = new ReservationController(reservationService);

        ConfirmReservationRequest request = new ConfirmReservationRequest();
        ConfirmReservationResponse expectedResponse = new ConfirmReservationResponse();

        when(reservationService.confirmReservation(request)).thenReturn(expectedResponse);

        ResponseEntity<ConfirmReservationResponse> response = controller.confirmReservation(request);

        assertThat(200).isEqualTo(response.getStatusCode().value());
        assertSame(expectedResponse, response.getBody());
        verify(reservationService).confirmReservation(request);
    }
}