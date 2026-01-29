package org.example.roomreservation.reservation.application.validators;

import org.example.roomreservation.api.model.ConfirmReservationRequest;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

@Component
public class ReservationDurationRequestValidator implements ReservationRequestValidator {
    @Override
    public void validate(ConfirmReservationRequest reservationRequest) {
        if (!reservationRequest.getEndDate().isAfter(reservationRequest.getStartDate())) {
            throw new ReservationRequestException("INVALID_DATE_RANGE", "endDate must be after startDate");
        }

        long days = ChronoUnit.DAYS.between(reservationRequest.getStartDate(), reservationRequest.getEndDate());
        if (days > 30) {
            throw new ReservationRequestException("MAX_STAY_EXCEEDED", "A room cannot be reserved for more than 30 days");
        }
    }
}
