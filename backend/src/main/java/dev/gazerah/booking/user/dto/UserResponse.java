package dev.gazerah.booking.user.dto;

import dev.gazerah.booking.user.Role;
import dev.gazerah.booking.user.User;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role
) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
