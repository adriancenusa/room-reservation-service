package org.example.roomreservation.reservation.infra.banktransfer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankTransferPaymentUpdateEvent {
    String paymentId;
    String debtorAccountNumber;
    BigDecimal amountReceived;
    String transactionDescription;
}
