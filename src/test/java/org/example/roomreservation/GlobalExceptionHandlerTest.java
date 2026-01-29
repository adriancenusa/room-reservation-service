package org.example.roomreservation;

import org.example.roomreservation.api.model.ErrorResponse;
import org.example.roomreservation.reservation.RoomReservationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private RoomReservationException roomReservationException;

    @Test
    void handleRoomReservationException_returnsStatusAndBodyFromException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        when(roomReservationException.getCode()).thenReturn("ERR_CODE");
        when(roomReservationException.getMessage()).thenReturn("validation failed");
        when(roomReservationException.getStatus()).thenReturn(HttpStatus.BAD_REQUEST);

        ResponseEntity<ErrorResponse> response = handler.handleRoomReservationException(roomReservationException);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("ERR_CODE");
        assertThat(response.getBody().getError()).isEqualTo("validation failed");
    }

    @Test
    void handleUnexpected_returnsInternalServerErrorWithGenericBody() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        RuntimeException unexpected = new RuntimeException("boom");

        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(unexpected);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(GlobalExceptionHandler.INTERNAL_ERROR);
        assertThat(response.getBody().getError()).isEqualTo(GlobalExceptionHandler.AN_UNEXPECTED_ERROR_OCCURRED);
    }
}