package org.example.roomreservation.reservation.infra.banktransfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.roomreservation.reservation.application.BankTransferPaymentUpdateHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankTransferPaymentUpdateConsumer {

    private final BankTransferPaymentUpdateHandler bankTransferPaymentUpdateHandler;

    @KafkaListener(
            topics = "bank-transfer-payment-update"
    )
    public void onMessage(BankTransferPaymentUpdateEvent event, Acknowledgment ack) {
        log.info("Received bank transfer payment update event: {}", event);
        bankTransferPaymentUpdateHandler.handle(event);
        ack.acknowledge();
    }
}
