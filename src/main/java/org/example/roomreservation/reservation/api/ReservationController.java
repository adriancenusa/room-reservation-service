package org.example.roomreservation.reservation.api;

import lombok.RequiredArgsConstructor;
import org.example.roomreservation.api.ReservationsApi;
import org.example.roomreservation.api.model.ConfirmReservationRequest;
import org.example.roomreservation.api.model.ConfirmReservationResponse;
import org.example.roomreservation.reservation.application.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class ReservationController implements ReservationsApi {
    private final ReservationService reservationService;

    @Override
    public ResponseEntity<ConfirmReservationResponse> confirmReservation(ConfirmReservationRequest confirmReservationRequest) {
        return ResponseEntity.ok(reservationService.confirmReservation(confirmReservationRequest));
    }
}
