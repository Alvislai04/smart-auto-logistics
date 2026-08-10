package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.*;
import com.fyp.autologistics.entity.AppUser;
import com.fyp.autologistics.entity.ShipmentPartyProfile;
import com.fyp.autologistics.entity.UserRoleOption;
import com.fyp.autologistics.enums.UserRole;
import com.fyp.autologistics.exception.BadRequestException;
import com.fyp.autologistics.exception.ResourceNotFoundException;
import com.fyp.autologistics.repository.AppUserRepository;
import com.fyp.autologistics.repository.ShipmentPartyProfileRepository;
import com.fyp.autologistics.repository.UserRoleOptionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class UserAccountService {

    private static final Pattern HAS_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern HAS_DIGIT = Pattern.compile("\\d");

    private final AppUserRepository appUserRepository;
    private final UserRoleOptionRepository userRoleOptionRepository;
    private final ShipmentPartyProfileRepository shipmentPartyProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(
            AppUserRepository appUserRepository,
            UserRoleOptionRepository userRoleOptionRepository,
            ShipmentPartyProfileRepository shipmentPartyProfileRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.userRoleOptionRepository = userRoleOptionRepository;
        this.shipmentPartyProfileRepository = shipmentPartyProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return appUserRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserAccountFormOptionsResponse getFormOptions() {
        List<UserRoleOptionResponse> roleOptions = userRoleOptionRepository
                .findByActiveTrueOrderBySortOrderAscDisplayNameAsc()
                .stream()
                .map(this::mapRoleOptionToResponse)
                .toList();

        return new UserAccountFormOptionsResponse(roleOptions);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("A user with this email already exists.");
        }

        UserRole role = resolveActiveRole(request.getRoleKey());
        validatePasswordConfirmation(request.getPassword(), request.getConfirmPassword());
        validatePasswordStrength(request.getPassword());
        validateCompanyProfile(role, request.getCompanyName(), request.getOperatingLocation(),
                request.getLocationLatitude(), request.getLocationLongitude());

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setActive(request.getActive() == null || request.getActive());
        applyProfileFields(user, request.getFullName(), request.getCompanyName(),
                request.getOperatingLocation(), request.getLocationLatitude(), request.getLocationLongitude());

        AppUser savedUser = appUserRepository.save(user);
        syncCompanyProfile(savedUser);

        return mapToResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request, String actingUserEmail) {
        AppUser user = findUserById(userId);
        UserRole role = resolveActiveRole(request.getRoleKey());

        preventRemovingOwnAdminAccess(user, role, request.getActive(), actingUserEmail);
        validateCompanyProfile(role, request.getCompanyName(), request.getOperatingLocation(),
                request.getLocationLatitude(), request.getLocationLongitude());

        user.setRole(role);
        user.setActive(Boolean.TRUE.equals(request.getActive()));
        applyProfileFields(user, request.getFullName(), request.getCompanyName(),
                request.getOperatingLocation(), request.getLocationLatitude(), request.getLocationLongitude());

        AppUser savedUser = appUserRepository.save(user);
        syncCompanyProfile(savedUser);

        return mapToResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentProfile(String email) {
        return mapToResponse(findUserByEmail(email));
    }

    @Transactional
    public UserResponse updateCurrentProfile(String email, UpdateProfileRequest request) {
        AppUser user = findUserByEmail(email);

        validateCompanyProfile(user.getRole(), request.getCompanyName(), request.getOperatingLocation(),
                request.getLocationLatitude(), request.getLocationLongitude());
        applyProfileFields(user, request.getFullName(), request.getCompanyName(),
                request.getOperatingLocation(), request.getLocationLatitude(), request.getLocationLongitude());

        AppUser savedUser = appUserRepository.save(user);
        syncCompanyProfile(savedUser);

        return mapToResponse(savedUser);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        AppUser user = findUserByEmail(email);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect.");
        }

        validatePasswordConfirmation(request.getNewPassword(), request.getConfirmPassword());
        validatePasswordStrength(request.getNewPassword());

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from the current password.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        appUserRepository.save(user);
    }

    private AppUser findUserById(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private AppUser findUserByEmail(String email) {
        return appUserRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private UserRole resolveActiveRole(String roleKey) {
        String normalizedRoleKey = normalizeRequiredText(roleKey, "Role")
                .toUpperCase(Locale.ROOT);

        userRoleOptionRepository.findByRoleKeyAndActiveTrue(normalizedRoleKey)
                .orElseThrow(() -> new BadRequestException(
                        "Selected user role is not configured in the database."
                ));

        try {
            return UserRole.valueOf(normalizedRoleKey);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Selected user role is not supported by the system.");
        }
    }

    private void preventRemovingOwnAdminAccess(
            AppUser targetUser,
            UserRole requestedRole,
            Boolean requestedActive,
            String actingUserEmail
    ) {
        if (!targetUser.getEmail().equalsIgnoreCase(normalizeEmail(actingUserEmail))) {
            return;
        }

        if (requestedRole != UserRole.ADMIN || !Boolean.TRUE.equals(requestedActive)) {
            throw new BadRequestException("You cannot remove admin access from your own account.");
        }
    }

    private void validateCompanyProfile(
            UserRole role,
            String companyName,
            String operatingLocation,
            Double latitude,
            Double longitude
    ) {
        if (role == UserRole.ADMIN) {
            validateCoordinatePair(latitude, longitude);
            return;
        }

        if (!StringUtils.hasText(companyName)) {
            throw new BadRequestException("Company name is required for non-admin users.");
        }

        if (!StringUtils.hasText(operatingLocation)) {
            throw new BadRequestException("Operating location is required for non-admin users.");
        }

        if (latitude == null || longitude == null) {
            throw new BadRequestException("Latitude and longitude are required for non-admin users.");
        }
    }

    private void validateCoordinatePair(Double latitude, Double longitude) {
        if ((latitude == null && longitude != null) || (latitude != null && longitude == null)) {
            throw new BadRequestException("Latitude and longitude must be provided together.");
        }
    }

    private void applyProfileFields(
            AppUser user,
            String fullName,
            String companyName,
            String operatingLocation,
            Double locationLatitude,
            Double locationLongitude
    ) {
        user.setFullName(normalizeRequiredText(fullName, "Full name"));
        user.setCompanyName(normalizeNullableText(companyName));
        user.setOperatingLocation(normalizeNullableText(operatingLocation));
        user.setLocationLatitude(locationLatitude);
        user.setLocationLongitude(locationLongitude);
    }

    private void syncCompanyProfile(AppUser user) {
        if (user.getRole() == UserRole.ADMIN) {
            deactivateLinkedPartyProfile(user);
            appUserRepository.save(user);
            return;
        }

        if (!StringUtils.hasText(user.getPartyProfileKey())) {
            user.setPartyProfileKey(generatePartyProfileKey(user));
            appUserRepository.save(user);
        }

        ShipmentPartyProfile profile = shipmentPartyProfileRepository
                .findByPartyKey(user.getPartyProfileKey())
                .orElseGet(ShipmentPartyProfile::new);

        profile.setPartyKey(user.getPartyProfileKey());
        profile.setPartyRole(user.getRole());
        profile.setCompanyName(user.getCompanyName());
        profile.setOperatingLocation(user.getOperatingLocation());
        profile.setLocationLatitude(user.getLocationLatitude());
        profile.setLocationLongitude(user.getLocationLongitude());
        profile.setActive(user.isActive());
        profile.setSortOrder(1000 + user.getId().intValue());

        shipmentPartyProfileRepository.save(profile);
    }

    private void deactivateLinkedPartyProfile(AppUser user) {
        String partyProfileKey = user.getPartyProfileKey();

        if (!StringUtils.hasText(partyProfileKey)) {
            return;
        }

        shipmentPartyProfileRepository.findByPartyKey(partyProfileKey)
                .ifPresent(profile -> {
                    profile.setActive(false);
                    shipmentPartyProfileRepository.save(profile);
                });

        user.setPartyProfileKey(null);
    }

    private String generatePartyProfileKey(AppUser user) {
        String prefix = switch (user.getRole()) {
            case MANUFACTURER -> "MFG";
            case LOGISTICS_PROVIDER -> "LSP";
            case DEALERSHIP -> "DLR";
            case ADMIN -> "ADM";
        };

        return prefix + "-USR-" + user.getId();
    }

    private void validatePasswordConfirmation(String password, String confirmPassword) {
        if (!StringUtils.hasText(password) || !password.equals(confirmPassword)) {
            throw new BadRequestException("Password confirmation does not match.");
        }
    }

    private void validatePasswordStrength(String password) {
        if (!StringUtils.hasText(password) || password.length() < 8 || password.length() > 72) {
            throw new BadRequestException("Password must be between 8 and 72 characters.");
        }

        if (!HAS_UPPERCASE.matcher(password).find()
                || !HAS_LOWERCASE.matcher(password).find()
                || !HAS_DIGIT.matcher(password).find()) {
            throw new BadRequestException(
                    "Password must include uppercase, lowercase, and numeric characters."
            );
        }
    }

    private String normalizeEmail(String email) {
        return normalizeRequiredText(email, "Email").toLowerCase(Locale.ROOT);
    }

    private String normalizeRequiredText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(fieldName + " is required.");
        }

        return value.trim();
    }

    private String normalizeNullableText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }

    private UserRoleOptionResponse mapRoleOptionToResponse(UserRoleOption roleOption) {
        return new UserRoleOptionResponse(
                roleOption.getRoleKey(),
                roleOption.getDisplayName(),
                roleOption.getDescription()
        );
    }

    private UserResponse mapToResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCompanyName(),
                user.getOperatingLocation(),
                user.getLocationLatitude(),
                user.getLocationLongitude(),
                user.getPartyProfileKey(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
