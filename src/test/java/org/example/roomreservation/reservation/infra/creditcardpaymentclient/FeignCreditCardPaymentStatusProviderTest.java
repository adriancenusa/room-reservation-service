package org.example.roomreservation.reservation.infra.creditcardpaymentclient;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.example.roomreservation.reservation.infra.creditcardpaymentclient.dto.CreditCardPaymentStatus;
import org.example.roomreservation.reservation.infra.creditcardpaymentclient.dto.PaymentStatusResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration",

})
class FeignCreditCardPaymentStatusProviderTest {

    @Autowired
    private FeignCreditCardPaymentStatusProvider provider;

    @MockitoBean
    private CreditCardPaymentFeignClient creditCardPaymentFeignClient;

    @MockitoBean
    private PaymentStatusResponse paymentStatusResponse;

    private FeignException buildFeignException() {
        Request request = Request.create(Request.HttpMethod.POST, "/payment-status", Collections.emptyMap(), null, StandardCharsets.UTF_8);
        Response response = Response.builder()
                .status(500)
                .reason("Server Error")
                .request(request)
                .build();
        return FeignException.errorStatus("operationKey", response);
    }

    @Test
    void getStatus_retriesUntilSuccess_and_callsFeignClient_threeTimes() {
        String paymentReference = "pay-ref-1";

        FeignException feignException = buildFeignException();

        when(creditCardPaymentFeignClient.retrievePaymentStatus(any()))
                .thenThrow(feignException)
                .thenThrow(feignException)
                .thenReturn(paymentStatusResponse);

        when(paymentStatusResponse.getStatus()).thenReturn(CreditCardPaymentStatus.CONFIRMED);

        var result = provider.getStatus(paymentReference);

        assertThat(result).isEqualTo(CreditCardPaymentStatus.CONFIRMED);
        verify(creditCardPaymentFeignClient, times(3)).retrievePaymentStatus(any());
    }

    @Test
    void getStatus_exhaustsRetries_and_fallbackThrowsServiceUnavailable() {
        String paymentReference = "pay-ref-2";

        FeignException feignException = buildFeignException();

        when(creditCardPaymentFeignClient.retrievePaymentStatus(any()))
                .thenThrow(feignException);

        assertThatThrownBy(() -> provider.getStatus(paymentReference))
                .isInstanceOf(CreditCardPaymentException.class)
                .hasMessageContaining("Credit card payment service unavailable");

        verify(creditCardPaymentFeignClient, times(3)).retrievePaymentStatus(any());
    }
}