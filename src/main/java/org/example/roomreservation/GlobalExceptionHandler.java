package org.example.roomreservation;

import lombok.extern.slf4j.Slf4j;
import org.example.roomreservation.api.model.ErrorResponse;
import org.example.roomreservation.reservation.RoomReservationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String AN_UNEXPECTED_ERROR_OCCURRED = "An unexpected error occurred";

    @ExceptionHandler(RoomReservationException.class)
    public ResponseEntity<ErrorResponse> handleRoomReservationException(RoomReservationException ex) {
        ErrorResponse body = new ErrorResponse(ex.getCode(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);

        ErrorResponse body = new ErrorResponse(INTERNAL_ERROR, AN_UNEXPECTED_ERROR_OCCURRED);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
