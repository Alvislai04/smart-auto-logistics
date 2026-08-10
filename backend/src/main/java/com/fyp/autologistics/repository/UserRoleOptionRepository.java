package com.fyp.autologistics.repository;

import com.fyp.autologistics.entity.UserRoleOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRoleOptionRepository extends JpaRepository<UserRoleOption, Long> {

    List<UserRoleOption> findByActiveTrueOrderBySortOrderAscDisplayNameAsc();

    Optional<UserRoleOption> findByRoleKeyAndActiveTrue(String roleKey);
}
