package com.fyp.autologistics.repository;

import com.fyp.autologistics.entity.SimulationProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SimulationProfileRepository extends JpaRepository<SimulationProfile, Long> {

    List<SimulationProfile> findAllByOrderBySortOrderAscDisplayNameAsc();

    List<SimulationProfile> findByActiveTrueOrderBySortOrderAscDisplayNameAsc();

    Optional<SimulationProfile> findByProfileKey(String profileKey);
}
