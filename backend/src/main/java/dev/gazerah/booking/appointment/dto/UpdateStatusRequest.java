package dev.gazerah.booking.appointment.dto;

import dev.gazerah.booking.appointment.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull AppointmentStatus status
) {
}
