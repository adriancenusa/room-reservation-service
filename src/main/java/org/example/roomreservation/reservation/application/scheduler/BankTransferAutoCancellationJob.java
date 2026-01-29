package org.example.roomreservation.reservation.application.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.roomreservation.api.model.PaymentMode;
import org.example.roomreservation.api.model.ReservationStatus;
import org.example.roomreservation.reservation.persistance.ReservationEntity;
import org.example.roomreservation.reservation.persistance.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BankTransferAutoCancellationJob {

    private static final Logger log = LoggerFactory.getLogger(BankTransferAutoCancellationJob.class);
    private static final ZoneId HOTEL_ZONE = ZoneId.of("Europe/Amsterdam");

    private final ReservationRepository reservationRepository;

    @Scheduled(cron = "0 5 1 * * *", zone = "Europe/Amsterdam")
    @Transactional
    public void cancelOverdueBankTransfers() {
        LocalDate today = LocalDate.now(HOTEL_ZONE);

        LocalDate cutoff = today.plusDays(2);

        List<ReservationEntity> candidates =
                reservationRepository.findByPaymentModeAndStatusAndStartDateLessThanEqual(
                        PaymentMode.BANK_TRANSFER,
                        ReservationStatus.PENDING_PAYMENT,
                        cutoff
                );

        if (candidates.isEmpty()) {
            return;
        }

        for (ReservationEntity r : candidates) {
            r.setStatus(ReservationStatus.CANCELLED);
            r.setUpdatedAt(OffsetDateTime.now());
            reservationRepository.save(r);
        }

        reservationRepository.flush();

        log.info("Auto-cancelled {} bank-transfer reservations with unpaid cutoff <= {}", candidates.size(), cutoff);
    }
}
