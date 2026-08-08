package com.fyp.autologistics.dto;

import com.fyp.autologistics.enums.UserRole;

public class LoginResponse {

    private String token;
    private Long userId;
    private String fullName;
    private String email;
    private UserRole role;

    public LoginResponse(
            String token,
            Long userId,
            String fullName,
            String email,
            UserRole role
    ) {
        this.token = token;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }
}