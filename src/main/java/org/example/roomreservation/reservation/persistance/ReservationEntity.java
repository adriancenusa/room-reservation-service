package org.example.roomreservation.reservation.persistance;

import jakarta.persistence.*;
import lombok.*;
import org.example.roomreservation.api.model.PaymentMode;
import org.example.roomreservation.api.model.ReservationStatus;
import org.example.roomreservation.api.model.RoomSegment;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservation")
@Builder
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationEntity {

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "reservation_id", length = 8, nullable = false, unique = true)
    private String reservationId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "room_number", nullable = false)
    private Integer roomNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_segment", nullable = false)
    private RoomSegment roomSegment;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false)
    private PaymentMode paymentMode;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
