package org.example.roomreservation.reservation.infra.creditcardpaymentclient;

import feign.FeignException;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.example.roomreservation.reservation.application.paymenthandler.CreditCardPaymentStatusProvider;
import org.example.roomreservation.reservation.infra.creditcardpaymentclient.dto.CreditCardPaymentStatus;
import org.example.roomreservation.reservation.infra.creditcardpaymentclient.dto.PaymentStatusResponse;
import org.example.roomreservation.reservation.infra.creditcardpaymentclient.dto.PaymentStatusRetrievalRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class FeignCreditCardPaymentStatusProvider implements CreditCardPaymentStatusProvider {
    private final CreditCardPaymentFeignClient creditCardPaymentFeignClient;


    @Retry(name = "creditCardPayment", fallbackMethod = "fallback")
    @Override
    public CreditCardPaymentStatus getStatus(String paymentReference) {
        try {
            PaymentStatusResponse paymentStatusResponse = creditCardPaymentFeignClient.retrievePaymentStatus(new PaymentStatusRetrievalRequest(paymentReference));
            return paymentStatusResponse.getStatus();
        } catch (FeignException e) {
            throw new CreditCardPaymentException("CREDIT_CARD_PAYMENT_SERVICE_EXCEPTION", e.getMessage());
        }
    }

    CreditCardPaymentStatus fallback(String paymentReference, Throwable t) {
        throw new CreditCardPaymentException(
                "CREDIT_CARD_SERVICE_UNAVAILABLE",
                HttpStatus.SERVICE_UNAVAILABLE,
                "Credit card payment service unavailable");
    }
}
