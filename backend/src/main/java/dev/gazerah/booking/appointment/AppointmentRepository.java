package dev.gazerah.booking.appointment;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @EntityGraph(attributePaths = {"service", "staff"})
    Optional<Appointment> findByIdAndTenantId(Long id, Long tenantId);

    @EntityGraph(attributePaths = {"service", "staff"})
    List<Appointment> findAllByTenantIdOrderByStartAtAsc(Long tenantId);

    List<Appointment> findAllByStaffIdAndTenantId(Long staffId, Long tenantId);

    boolean existsByStaffIdAndTenantIdAndStatusNotAndStartAtBeforeAndEndAtAfter(
            Long staffId, Long tenantId, AppointmentStatus status, Instant end, Instant start);
}
