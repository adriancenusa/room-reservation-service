// src/test/java/org/example/roomreservation/reservation/application/paymenthandler/PaymentModeRegistryTest.java
package org.example.roomreservation.reservation.application.paymenthandler;

import org.example.roomreservation.api.model.PaymentMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class PaymentModeRegistryTest {

    @Mock
    private CreditCardPaymentModeHandler creditCardPaymentModeHandler;

    @Mock
    private CashPaymentModeHandler cashPaymentModeHandler;

    @Mock
    private BankTransferPaymentModeHandler bankTransferPaymentModeHandler;

    @Test
    void getHandler_returnsProvidedHandlersForKnownModes() {
        PaymentModeRegistry registry = new PaymentModeRegistry(
                creditCardPaymentModeHandler,
                cashPaymentModeHandler,
                bankTransferPaymentModeHandler
        );

        assertThat(registry.getHandler(PaymentMode.CREDIT_CARD)).isSameAs(creditCardPaymentModeHandler);
        assertThat(registry.getHandler(PaymentMode.CASH)).isSameAs(cashPaymentModeHandler);
        assertThat(registry.getHandler(PaymentMode.BANK_TRANSFER)).isSameAs(bankTransferPaymentModeHandler);
    }

 
}