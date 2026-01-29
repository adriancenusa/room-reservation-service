package org.example.roomreservation.reservation.application.paymenthandler;

import org.example.roomreservation.api.model.PaymentMode;
import org.springframework.stereotype.Component;

import java.util.EnumMap;

@Component
public class PaymentModeRegistry {

    private final EnumMap<PaymentMode, PaymentModeHandler> registry;

    public PaymentModeRegistry(CreditCardPaymentModeHandler creditCardPaymentModeHandler, CashPaymentModeHandler cashPaymentModeHandler, BankTransferPaymentModeHandler bankTransferPaymentModeHandler) {
        this.registry = new EnumMap<>(PaymentMode.class);

        registry.put(PaymentMode.CREDIT_CARD, creditCardPaymentModeHandler);
        registry.put(PaymentMode.CASH, cashPaymentModeHandler);
        registry.put(PaymentMode.BANK_TRANSFER, bankTransferPaymentModeHandler);

        for (PaymentMode mode : PaymentMode.values()) {
            if (!registry.containsKey(mode)) {
                throw new IllegalStateException("No PaymentModeHandler registered for " + mode);
            }
        }
    }

    public PaymentModeHandler getHandler(PaymentMode paymentMode) {
        return registry.get(paymentMode);
    }
}
