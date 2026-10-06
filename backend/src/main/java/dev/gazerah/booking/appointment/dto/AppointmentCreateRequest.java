package dev.gazerah.booking.appointment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record AppointmentCreateRequest(
        @NotNull Long serviceId,
        @NotNull Long staffId,
        @NotBlank @Size(max = 255) String customerName,
        @Email @Size(max = 255) String customerEmail,
        @Size(max = 255) String customerPhone,
        @NotNull Instant startAt,
        @NotNull Instant endAt
) {
}
