package dev.gazerah.booking.user.dto;

import dev.gazerah.booking.user.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
        @NotNull Role role
) {
}
