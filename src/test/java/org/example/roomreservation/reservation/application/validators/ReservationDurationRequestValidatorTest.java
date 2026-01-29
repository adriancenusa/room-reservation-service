package org.example.roomreservation.reservation.application.validators;

import org.example.roomreservation.api.model.ConfirmReservationRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationDurationRequestValidatorTest {

    private final ReservationDurationRequestValidator validator = new ReservationDurationRequestValidator();

    @Test
    void validate_doesNotThrow_forValidRangeWithin30Days() {
        ConfirmReservationRequest req = new ConfirmReservationRequest();
        req.setStartDate(LocalDate.of(2025, 1, 1));
        req.setEndDate(LocalDate.of(2025, 1, 31)); // 30 days

        assertThatCode(() -> validator.validate(req))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_throwsWhenEndDateNotAfterStartDate() {
        ConfirmReservationRequest req = new ConfirmReservationRequest();
        req.setStartDate(LocalDate.of(2025, 1, 10));
        req.setEndDate(LocalDate.of(2025, 1, 10)); // not after

        assertThatThrownBy(() -> validator.validate(req))
                .isInstanceOf(ReservationRequestException.class)
                .hasMessageContaining("endDate must be after startDate");
    }

    @Test
    void validate_throwsWhenStayExceeds30Days() {
        ConfirmReservationRequest req = new ConfirmReservationRequest();
        req.setStartDate(LocalDate.of(2025, 1, 1));
        req.setEndDate(LocalDate.of(2025, 2, 2)); // 32 days

        assertThatThrownBy(() -> validator.validate(req))
                .isInstanceOf(ReservationRequestException.class)
                .hasMessageContaining("A room cannot be reserved for more than 30 days");
    }
}