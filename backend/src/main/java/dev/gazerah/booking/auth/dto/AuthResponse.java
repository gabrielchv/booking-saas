package dev.gazerah.booking.auth.dto;

import dev.gazerah.booking.user.Role;

public record AuthResponse(
        String token,
        Long userId,
        Long tenantId,
        String fullName,
        String email,
        Role role
) {
}
