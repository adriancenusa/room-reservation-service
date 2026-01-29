package org.example.roomreservation.reservation.infra.creditcardpaymentclient;

import feign.Logger;
import feign.Request;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

public class CreditCardPaymentFeignConfig {

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(1, TimeUnit.SECONDS, 2, TimeUnit.SECONDS, true);
    }


    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }


}
