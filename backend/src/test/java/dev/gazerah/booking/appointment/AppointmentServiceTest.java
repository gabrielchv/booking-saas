package dev.gazerah.booking.appointment;

import dev.gazerah.booking.appointment.dto.AppointmentCreateRequest;
import dev.gazerah.booking.catalog.Offering;
import dev.gazerah.booking.catalog.OfferingRepository;
import dev.gazerah.booking.common.BadRequestException;
import dev.gazerah.booking.common.ConflictException;
import dev.gazerah.booking.common.NotFoundException;
import dev.gazerah.booking.common.TenantContext;
import dev.gazerah.booking.user.Role;
import dev.gazerah.booking.user.User;
import dev.gazerah.booking.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    private static final Long TENANT = 1L;
    private static final Instant START = Instant.parse("2026-11-01T10:00:00Z");
    private static final Instant END = Instant.parse("2026-11-01T11:00:00Z");

    @Mock
    private AppointmentRepository repository;

    @Mock
    private OfferingRepository offeringRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppointmentService service;

    @BeforeEach
    void setUp() {
        TenantContext.set(TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void rejectsEndBeforeStart() {
        AppointmentCreateRequest request = new AppointmentCreateRequest(1L, 1L, "Ana", null, null, END, START);

        assertThrows(BadRequestException.class, () -> service.create(request));
    }

    @Test
    void rejectsUnknownService() {
        AppointmentCreateRequest request = new AppointmentCreateRequest(9L, 1L, "Ana", null, null, START, END);
        when(offeringRepository.findByIdAndTenantId(9L, TENANT)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(request));
    }

    @Test
    void rejectsUnknownStaff() {
        AppointmentCreateRequest request = new AppointmentCreateRequest(1L, 9L, "Ana", null, null, START, END);
        when(offeringRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(offering()));
        when(userRepository.findByIdAndTenantId(9L, TENANT)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(request));
    }

    @Test
    void rejectsOverlappingBooking() {
        AppointmentCreateRequest request = new AppointmentCreateRequest(1L, 1L, "Ana", null, null, START, END);
        when(offeringRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(offering()));
        when(userRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(staff()));
        when(repository.existsByStaffIdAndTenantIdAndStatusNotAndStartAtBeforeAndEndAtAfter(
                1L, TENANT, AppointmentStatus.CANCELLED, END, START)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(request));
    }

    @Test
    void createsAppointmentWhenFree() {
        AppointmentCreateRequest request = new AppointmentCreateRequest(1L, 1L, "Ana", null, null, START, END);
        when(offeringRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(offering()));
        when(userRepository.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(staff()));
        when(repository.existsByStaffIdAndTenantIdAndStatusNotAndStartAtBeforeAndEndAtAfter(
                1L, TENANT, AppointmentStatus.CANCELLED, END, START)).thenReturn(false);
        when(repository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(request);

        assertEquals("Ana", response.customerName());
        assertEquals(AppointmentStatus.SCHEDULED, response.status());
        verify(repository).save(any(Appointment.class));
    }

    private Offering offering() {
        return new Offering(TENANT, "Cut", null, 30, BigDecimal.TEN);
    }

    private User staff() {
        return new User(TENANT, "Staff", "staff@example.com", "hash", Role.STAFF);
    }
}
