package com.fyp.autologistics.repository;

import com.fyp.autologistics.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    List<AppUser> findAllByOrderByCreatedAtDesc();

    boolean existsByEmailIgnoreCase(String email);
}
