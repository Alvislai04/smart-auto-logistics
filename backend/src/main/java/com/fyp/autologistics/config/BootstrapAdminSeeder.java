package com.fyp.autologistics.config;

import com.fyp.autologistics.entity.AppUser;
import com.fyp.autologistics.enums.UserRole;
import com.fyp.autologistics.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Component
public class BootstrapAdminSeeder implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean bootstrapAdminEnabled;
    private final String bootstrapAdminFullName;
    private final String bootstrapAdminEmail;
    private final String bootstrapAdminPassword;

    public BootstrapAdminSeeder(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.enabled:false}") boolean bootstrapAdminEnabled,
            @Value("${app.bootstrap-admin.full-name:}") String bootstrapAdminFullName,
            @Value("${app.bootstrap-admin.email:}") String bootstrapAdminEmail,
            @Value("${app.bootstrap-admin.password:}") String bootstrapAdminPassword
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapAdminEnabled = bootstrapAdminEnabled;
        this.bootstrapAdminFullName = bootstrapAdminFullName;
        this.bootstrapAdminEmail = bootstrapAdminEmail;
        this.bootstrapAdminPassword = bootstrapAdminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!bootstrapAdminEnabled) {
            return;
        }

        if (!StringUtils.hasText(bootstrapAdminFullName)
                || !StringUtils.hasText(bootstrapAdminEmail)
                || !StringUtils.hasText(bootstrapAdminPassword)) {
            return;
        }

        upsertBootstrapAdmin();
    }

    private void upsertBootstrapAdmin() {
        String normalizedEmail = bootstrapAdminEmail.trim().toLowerCase(Locale.ROOT);

        AppUser user = appUserRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(AppUser::new);

        if (user.getId() == null) {
            user.setEmail(normalizedEmail);
            user.setPasswordHash(passwordEncoder.encode(bootstrapAdminPassword));
        }

        user.setFullName(bootstrapAdminFullName.trim());
        user.setRole(UserRole.ADMIN);
        user.setActive(true);

        appUserRepository.save(user);
    }
}
