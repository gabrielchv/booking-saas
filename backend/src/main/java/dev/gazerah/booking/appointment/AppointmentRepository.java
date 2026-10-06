package dev.gazerah.booking.appointment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByIdAndTenantId(Long id, Long tenantId);

    List<Appointment> findAllByTenantIdOrderByStartAtAsc(Long tenantId);

    List<Appointment> findAllByStaffIdAndTenantId(Long staffId, Long tenantId);

    boolean existsByStaffIdAndTenantIdAndStatusNotAndStartAtBeforeAndEndAtAfter(
            Long staffId, Long tenantId, AppointmentStatus status, Instant end, Instant start);
}
