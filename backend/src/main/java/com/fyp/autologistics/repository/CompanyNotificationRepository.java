package com.fyp.autologistics.repository;

import com.fyp.autologistics.entity.CompanyNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyNotificationRepository extends JpaRepository<CompanyNotification, Long> {

    List<CompanyNotification> findAllByOrderBySentAtDesc();

    boolean existsByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
}