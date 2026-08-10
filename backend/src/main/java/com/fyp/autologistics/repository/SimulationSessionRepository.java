package com.fyp.autologistics.repository;

import com.fyp.autologistics.entity.SimulationSession;
import com.fyp.autologistics.enums.SimulationSessionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SimulationSessionRepository extends JpaRepository<SimulationSession, Long> {

    @EntityGraph(attributePaths = "shipment")
    List<SimulationSession> findByStatusAndNextRunAtLessThanEqualOrderByNextRunAtAsc(
            SimulationSessionStatus status,
            LocalDateTime nextRunAt
    );

    @EntityGraph(attributePaths = "shipment")
    List<SimulationSession> findByStatusOrderByUpdatedAtDesc(SimulationSessionStatus status);

    @EntityGraph(attributePaths = "shipment")
    List<SimulationSession> findAllByOrderByUpdatedAtDesc();

    @EntityGraph(attributePaths = "shipment")
    Optional<SimulationSession> findFirstByShipmentIdAndStatusInOrderByCreatedAtDesc(
            Long shipmentId,
            Collection<SimulationSessionStatus> statuses
    );

    @EntityGraph(attributePaths = "shipment")
    Optional<SimulationSession> findFirstByShipmentIdOrderByUpdatedAtDesc(Long shipmentId);
}
