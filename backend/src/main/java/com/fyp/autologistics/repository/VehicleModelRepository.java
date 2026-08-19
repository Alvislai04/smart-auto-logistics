package com.fyp.autologistics.repository;

import com.fyp.autologistics.entity.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {

    List<VehicleModel> findByActiveTrueOrderBySortOrderAscDisplayNameAsc();

    List<VehicleModel> findAllByOrderBySortOrderAscDisplayNameAsc();

    Optional<VehicleModel> findByModelCodeAndActiveTrue(String modelCode);

    Optional<VehicleModel> findByModelCode(String modelCode);
}
