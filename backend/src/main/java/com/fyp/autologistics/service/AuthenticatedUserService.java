package com.fyp.autologistics.service;

import com.fyp.autologistics.entity.AppUser;
import com.fyp.autologistics.exception.ResourceNotFoundException;
import com.fyp.autologistics.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Service
public class AuthenticatedUserService {

    private final AppUserRepository appUserRepository;

    public AuthenticatedUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUser getAuthenticatedUser(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ResourceNotFoundException("Authenticated user not found.");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        return appUserRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found."
                ));
    }
}
