package com.fyp.autologistics.repository;

import com.fyp.autologistics.entity.Shipment;
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

    long countByManufacturerKey(String manufacturerKey);

    long countByManufacturerKeyAndStatus(String manufacturerKey, ShipmentStatus status);

    long countByDealershipKey(String dealershipKey);

    long countByDealershipKeyAndStatus(String dealershipKey, ShipmentStatus status);

    List<Shipment> findByStatusIn(Collection<ShipmentStatus> statuses);

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
}
