package com.fyp.autologistics.dto;

import java.util.List;

public record UserAccountFormOptionsResponse(
        List<UserRoleOptionResponse> roleOptions
) {
}
