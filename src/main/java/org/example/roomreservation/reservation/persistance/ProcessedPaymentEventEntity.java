package org.example.roomreservation.reservation.persistance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "processed_payment_event")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedPaymentEventEntity {

    @Id
    @Column(name = "payment_id", length = 100, nullable = false)
    private String paymentId;

    @Column(name = "processed_at", nullable = false)
    private OffsetDateTime processedAt;


}
