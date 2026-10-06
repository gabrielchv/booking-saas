package dev.gazerah.booking.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OfferingRepository extends JpaRepository<Offering, Long> {

    List<Offering> findAllByTenantId(Long tenantId);

    Optional<Offering> findByIdAndTenantId(Long id, Long tenantId);
}
