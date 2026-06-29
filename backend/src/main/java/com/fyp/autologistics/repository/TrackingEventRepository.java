package com.fyp.autologistics.repository;

import com.fyp.autologistics.entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {

    List<TrackingEvent> findByShipmentIdOrderByRecordedAtAsc(Long shipmentId);

    Optional<TrackingEvent> findFirstByShipmentIdOrderByRecordedAtDesc(Long shipmentId);
}