package dev.gazerah.booking.catalog;

import dev.gazerah.booking.catalog.dto.OfferingRequest;
import dev.gazerah.booking.catalog.dto.OfferingResponse;
import dev.gazerah.booking.common.NotFoundException;
import dev.gazerah.booking.common.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OfferingService {

    private final OfferingRepository repository;

    public OfferingService(OfferingRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<OfferingResponse> list() {
        return repository.findAllByTenantId(TenantContext.require())
                .stream()
                .map(OfferingResponse::from)
                .toList();
    }

    @Transactional
    public OfferingResponse create(OfferingRequest request) {
        Offering offering = new Offering(
                TenantContext.require(),
                request.name(),
                request.description(),
                request.durationMinutes(),
                request.price());
        return OfferingResponse.from(repository.save(offering));
    }

    @Transactional
    public OfferingResponse update(Long id, OfferingRequest request) {
        Offering offering = repository.findByIdAndTenantId(id, TenantContext.require())
                .orElseThrow(() -> new NotFoundException("Service not found"));
        offering.setName(request.name());
        offering.setDescription(request.description());
        offering.setDurationMinutes(request.durationMinutes());
        offering.setPrice(request.price());
        return OfferingResponse.from(offering);
    }

    @Transactional
    public void delete(Long id) {
        Offering offering = repository.findByIdAndTenantId(id, TenantContext.require())
                .orElseThrow(() -> new NotFoundException("Service not found"));
        repository.delete(offering);
    }
}
