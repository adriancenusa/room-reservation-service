package org.example.roomreservation.reservation.infra.creditcardpaymentclient;

import org.example.roomreservation.reservation.infra.creditcardpaymentclient.dto.PaymentStatusResponse;
import org.example.roomreservation.reservation.infra.creditcardpaymentclient.dto.PaymentStatusRetrievalRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "creditCardPaymentClient",
        url = "${clients.credit-card.base-url}",
        configuration = CreditCardPaymentFeignConfig.class)
public interface CreditCardPaymentFeignClient {

    @PostMapping(
            value = "/payment-status",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    PaymentStatusResponse retrievePaymentStatus(@RequestBody PaymentStatusRetrievalRequest request);

}
