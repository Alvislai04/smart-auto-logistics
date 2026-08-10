package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.CreateUserRequest;
import com.fyp.autologistics.dto.UpdateUserRequest;
import com.fyp.autologistics.dto.UserAccountFormOptionsResponse;
import com.fyp.autologistics.dto.UserResponse;
import com.fyp.autologistics.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminUserController {

    private final UserAccountService userAccountService;

    public AdminUserController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping
    public List<UserResponse> getUsers() {
        return userAccountService.getAllUsers();
    }

    @GetMapping("/form-options")
    public UserAccountFormOptionsResponse getFormOptions() {
        return userAccountService.getFormOptions();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return userAccountService.createUser(request);
    }

    @PutMapping("/{userId}")
    public UserResponse updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication
    ) {
        return userAccountService.updateUser(userId, request, authentication.getName());
    }
}
