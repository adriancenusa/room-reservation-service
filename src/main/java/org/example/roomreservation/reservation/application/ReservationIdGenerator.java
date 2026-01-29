package org.example.roomreservation.reservation.application;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ReservationIdGenerator {
    private static final char[] BASE62 =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    public String nextId() {
        char[] buf = new char[8];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = BASE62[RANDOM.nextInt(BASE62.length)];
        }
        return new String(buf);
    }
}
