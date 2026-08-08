package com.fyp.autologistics.specification;

import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.ShipmentStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class ShipmentSpecifications {

    private ShipmentSpecifications() {
    }

    public static Specification<Shipment> matchesFilters(
            String searchTerm,
            ShipmentStatus status,
            LogisticsStage stage
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(searchTerm)) {
                String normalizedSearchTerm =
                        "%" + searchTerm.trim().toLowerCase() + "%";

                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("shipmentCode")),
                                normalizedSearchTerm
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("vehicleVin")),
                                normalizedSearchTerm
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("vehicleModel")),
                                normalizedSearchTerm
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("origin")),
                                normalizedSearchTerm
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("destination")),
                                normalizedSearchTerm
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("dealershipName")),
                                normalizedSearchTerm
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("responsiblePerson")),
                                normalizedSearchTerm
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("stockLocation")),
                                normalizedSearchTerm
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("customerName")),
                                normalizedSearchTerm
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("shipmentDocumentReference")),
                                normalizedSearchTerm
                        )
                ));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (stage != null) {
                predicates.add(criteriaBuilder.equal(root.get("currentStage"), stage));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
