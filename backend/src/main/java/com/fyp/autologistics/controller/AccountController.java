package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.ChangePasswordRequest;
import com.fyp.autologistics.dto.UpdateProfileRequest;
import com.fyp.autologistics.dto.UserResponse;
import com.fyp.autologistics.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
@CrossOrigin(origins = "http://localhost:5173")
public class AccountController {

    private final UserAccountService userAccountService;

    public AccountController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping("/profile")
    public UserResponse getProfile(Authentication authentication) {
        return userAccountService.getCurrentProfile(authentication.getName());
    }

    @PutMapping("/profile")
    public UserResponse updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication
    ) {
        return userAccountService.updateCurrentProfile(authentication.getName(), request);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        userAccountService.changePassword(authentication.getName(), request);
    }
}
