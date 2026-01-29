package org.example.roomreservation.reservation;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RoomReservationException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public RoomReservationException(String code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

}
