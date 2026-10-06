package dev.gazerah.booking.catalog.dto;

import dev.gazerah.booking.catalog.Offering;

import java.math.BigDecimal;

public record OfferingResponse(
        Long id,
        String name,
        String description,
        Integer durationMinutes,
        BigDecimal price
) {

    public static OfferingResponse from(Offering offering) {
        return new OfferingResponse(
                offering.getId(),
                offering.getName(),
                offering.getDescription(),
                offering.getDurationMinutes(),
                offering.getPrice());
    }
}
