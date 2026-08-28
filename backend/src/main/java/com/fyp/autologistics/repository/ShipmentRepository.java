package com.fyp.autologistics.repository;

import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ShipmentRepository
        extends JpaRepository<Shipment, Long>, JpaSpecificationExecutor<Shipment> {

    interface ShipmentStatusCount {
        ShipmentStatus getStatus();

        Long getTotal();
    }

    long countByStatus(ShipmentStatus status);

    @Query("""
            select s
            from Shipment s
            where s.status in :shipmentStatuses
              and s.originLatitude is not null
              and s.originLongitude is not null
              and s.destinationLatitude is not null
              and s.destinationLongitude is not null
              and not exists (
                  select 1
                  from SimulationSession ss
                  where ss.shipment = s
              )
            """)
    List<Shipment> findShipmentsWithoutSimulationSession(
            @Param("shipmentStatuses") Collection<ShipmentStatus> shipmentStatuses
    );

    @Query("""
            select s.status as status, count(s) as total
            from Shipment s
            group by s.status
            """)
    List<ShipmentStatusCount> countShipmentsByStatus();

    @Query("""
            select s.status as status, count(s) as total
            from Shipment s
            where s.manufacturerKey = :manufacturerKey
            group by s.status
            """)
    List<ShipmentStatusCount> countShipmentsByManufacturerKeyGrouped(
            @Param("manufacturerKey") String manufacturerKey
    );

    @Query("""
            select s.status as status, count(s) as total
            from Shipment s
            where s.dealershipKey = :dealershipKey
            group by s.status
            """)
    List<ShipmentStatusCount> countShipmentsByDealershipKeyGrouped(
            @Param("dealershipKey") String dealershipKey
    );

    boolean existsByShipmentCode(String shipmentCode);

    long countByManufacturerKeyAndVehicleModelCodeAndCurrentStageAndStatusNot(
            String manufacturerKey,
            String vehicleModelCode,
            LogisticsStage currentStage,
            ShipmentStatus excludedStatus
    );

    long countByDealershipKeyAndVehicleModelCodeAndCurrentStageAndStatusNot(
            String dealershipKey,
            String vehicleModelCode,
            LogisticsStage currentStage,
            ShipmentStatus excludedStatus
    );
}
