package dev.gazerah.booking.appointment.dto;

import dev.gazerah.booking.appointment.Appointment;
import dev.gazerah.booking.appointment.AppointmentStatus;

import java.time.Instant;

public record AppointmentResponse(
        Long id,
        Long serviceId,
        String serviceName,
        Long staffId,
        String staffName,
        String customerName,
        String customerEmail,
        String customerPhone,
        Instant startAt,
        Instant endAt,
        AppointmentStatus status
) {

    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getService().getId(),
                appointment.getService().getName(),
                appointment.getStaff().getId(),
                appointment.getStaff().getFullName(),
                appointment.getCustomerName(),
                appointment.getCustomerEmail(),
                appointment.getCustomerPhone(),
                appointment.getStartAt(),
                appointment.getEndAt(),
                appointment.getStatus());
    }
}
