 package org.example.roomreservation.reservation.application;

import org.example.roomreservation.reservation.application.ReservationIdGenerator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationIdGeneratorTest {

    private static final String BASE62_REGEX = "^[0-9A-Za-z]{8}$";

    @Test
    void nextId_hasLength8_andMatchesBase62Charset() {
        ReservationIdGenerator generator = new ReservationIdGenerator();

        String id = generator.nextId();

        assertThat(id).isNotNull();
        assertThat(id).hasSize(8);
        assertThat(id).matches(BASE62_REGEX);
    }

    @Test
    void manyGeneratedIds_areUnique() {
        ReservationIdGenerator generator = new ReservationIdGenerator();
        int count = 1000;

        Set<String> ids = IntStream.range(0, count)
                .mapToObj(i -> generator.nextId())
                .collect(Collectors.toSet());

        assertThat(ids).hasSize(count);
    }

}