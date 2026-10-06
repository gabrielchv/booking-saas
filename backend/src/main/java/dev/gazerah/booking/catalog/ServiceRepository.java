package dev.gazerah.booking.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findAllByTenantId(Long tenantId);

    Optional<Service> findByIdAndTenantId(Long id, Long tenantId);
}
