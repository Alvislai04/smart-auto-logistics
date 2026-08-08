package com.fyp.autologistics.config;

import com.fyp.autologistics.entity.AppUser;
import com.fyp.autologistics.enums.UserRole;
import com.fyp.autologistics.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        upsertSeedUser(
                "System Administrator",
                "admin@autologistics.com",
                "Admin@12345",
                UserRole.ADMIN
        );

        upsertSeedUser(
                "Manufacturer Operator",
                "manufacturer@autologistics.com",
                "Manufacturer@12345",
                UserRole.MANUFACTURER
        );

        upsertSeedUser(
                "Logistics Operator",
                "logistics@autologistics.com",
                "Logistics@12345",
                UserRole.LOGISTICS_PROVIDER
        );

        upsertSeedUser(
                "Dealership Operator",
                "dealer@autologistics.com",
                "Dealer@12345",
                UserRole.DEALERSHIP
        );
    }

    private void upsertSeedUser(
            String fullName,
            String email,
            String rawPassword,
            UserRole role
    ) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseGet(AppUser::new);

        if (user.getId() == null) {
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
        }

        user.setFullName(fullName);
        user.setRole(role);
        user.setActive(true);

        appUserRepository.save(user);
    }
}
