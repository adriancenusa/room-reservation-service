package org.example.roomreservation.reservation.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedPaymentEventRepository extends JpaRepository<ProcessedPaymentEventEntity, String> {}
