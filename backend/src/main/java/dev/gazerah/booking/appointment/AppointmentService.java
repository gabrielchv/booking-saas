package dev.gazerah.booking.appointment;

import dev.gazerah.booking.appointment.dto.AppointmentCreateRequest;
import dev.gazerah.booking.appointment.dto.AppointmentResponse;
import dev.gazerah.booking.catalog.Offering;
import dev.gazerah.booking.catalog.OfferingRepository;
import dev.gazerah.booking.common.BadRequestException;
import dev.gazerah.booking.common.ConflictException;
import dev.gazerah.booking.common.NotFoundException;
import dev.gazerah.booking.common.TenantContext;
import dev.gazerah.booking.user.User;
import dev.gazerah.booking.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository repository;
    private final OfferingRepository offeringRepository;
    private final UserRepository userRepository;

    public AppointmentService(AppointmentRepository repository,
                              OfferingRepository offeringRepository,
                              UserRepository userRepository) {
        this.repository = repository;
        this.offeringRepository = offeringRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> list() {
        return repository.findAllByTenantIdOrderByStartAtAsc(TenantContext.require())
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponse get(Long id) {
        return AppointmentResponse.from(repository.findByIdAndTenantId(id, TenantContext.require())
                .orElseThrow(() -> new NotFoundException("Appointment not found")));
    }

    @Transactional
    public AppointmentResponse create(AppointmentCreateRequest request) {
        Long tenantId = TenantContext.require();

        if (!request.endAt().isAfter(request.startAt())) {
            throw new BadRequestException("endAt must be after startAt");
        }

        Offering offering = offeringRepository.findByIdAndTenantId(request.serviceId(), tenantId)
                .orElseThrow(() -> new NotFoundException("Service not found"));
        User staff = userRepository.findByIdAndTenantId(request.staffId(), tenantId)
                .orElseThrow(() -> new NotFoundException("Staff member not found"));

        boolean overlaps = repository
                .existsByStaffIdAndTenantIdAndStatusNotAndStartAtBeforeAndEndAtAfter(
                        request.staffId(), tenantId, AppointmentStatus.CANCELLED,
                        request.endAt(), request.startAt());
        if (overlaps) {
            throw new ConflictException("Staff member already has a booking in this window");
        }

        Appointment appointment = new Appointment(
                tenantId, offering, staff, request.customerName(),
                request.customerEmail(), request.customerPhone(),
                request.startAt(), request.endAt());
        return AppointmentResponse.from(repository.save(appointment));
    }

    @Transactional
    public AppointmentResponse updateStatus(Long id, AppointmentStatus status) {
        Appointment appointment = repository.findByIdAndTenantId(id, TenantContext.require())
                .orElseThrow(() -> new NotFoundException("Appointment not found"));
        appointment.setStatus(status);
        return AppointmentResponse.from(appointment);
    }

    @Transactional
    public void delete(Long id) {
        Appointment appointment = repository.findByIdAndTenantId(id, TenantContext.require())
                .orElseThrow(() -> new NotFoundException("Appointment not found"));
        repository.delete(appointment);
    }
}
