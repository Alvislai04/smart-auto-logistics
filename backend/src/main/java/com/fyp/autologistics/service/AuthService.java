package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.LoginRequest;
import com.fyp.autologistics.dto.LoginResponse;
import com.fyp.autologistics.entity.AppUser;
import com.fyp.autologistics.exception.BadRequestException;
import com.fyp.autologistics.repository.AppUserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;

    public AuthService(
            AuthenticationManager authenticationManager,
            CustomUserDetailsService customUserDetailsService,
            JwtService jwtService,
            AppUserRepository appUserRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtService = jwtService;
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtService.generateToken(userDetails);

        AppUser appUser = appUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password."));

        return new LoginResponse(
                token,
                appUser.getId(),
                appUser.getFullName(),
                appUser.getEmail(),
                appUser.getRole()
        );
    }
}