package com.fyp.autologistics.specification;

import com.fyp.autologistics.entity.Alert;
import com.fyp.autologistics.entity.AppUser;
import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.enums.AlertSeverity;
import com.fyp.autologistics.enums.AlertStatus;
import com.fyp.autologistics.enums.AlertType;
import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.enums.UserRole;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ReportSpecifications {

    private ReportSpecifications() {
    }

    public static Specification<Shipment> shipmentReport(
            AppUser user,
            String searchTerm,
            ShipmentStatus status,
            LogisticsStage stage,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            addShipmentScopePredicate(predicates, root, criteriaBuilder, user);

            if (StringUtils.hasText(searchTerm)) {
                String normalizedSearchTerm = "%" + searchTerm.trim().toLowerCase() + "%";

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
                        )
                ));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (stage != null) {
                predicates.add(criteriaBuilder.equal(root.get("currentStage"), stage));
            }

            addDateRangePredicate(predicates, root.get("createdAt"), criteriaBuilder, from, to);

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<Alert> alertReport(
            AppUser user,
            String searchTerm,
            AlertType alertType,
            AlertStatus status,
            AlertSeverity severity,
            Collection<AlertType> allowedAlertTypes,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Alert, Shipment> shipmentJoin = root.join("shipment");

            addAlertScopePredicate(predicates, shipmentJoin, criteriaBuilder, user);

            if (StringUtils.hasText(searchTerm)) {
                String normalizedSearchTerm = "%" + searchTerm.trim().toLowerCase() + "%";

                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("title")),
                                normalizedSearchTerm
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("message")),
                                normalizedSearchTerm
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(shipmentJoin.get("shipmentCode")),
                                normalizedSearchTerm
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(shipmentJoin.get("vehicleVin")),
                                normalizedSearchTerm
                        )
                ));
            }

            if (alertType != null) {
                predicates.add(criteriaBuilder.equal(root.get("alertType"), alertType));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (severity != null) {
                predicates.add(criteriaBuilder.equal(root.get("severity"), severity));
            }

            if (allowedAlertTypes != null && !allowedAlertTypes.isEmpty()) {
                predicates.add(root.get("alertType").in(allowedAlertTypes));
            }

            addDateRangePredicate(predicates, root.get("createdAt"), criteriaBuilder, from, to);

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void addShipmentScopePredicate(
            List<Predicate> predicates,
            jakarta.persistence.criteria.Root<Shipment> root,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            AppUser user
    ) {
        if (user.getRole() == UserRole.MANUFACTURER) {
            predicates.add(hasScopedKey(
                    criteriaBuilder,
                    root.get("manufacturerKey"),
                    user.getPartyProfileKey()
            ));
        }

        if (user.getRole() == UserRole.DEALERSHIP) {
            predicates.add(hasScopedKey(
                    criteriaBuilder,
                    root.get("dealershipKey"),
                    user.getPartyProfileKey()
            ));
        }
    }

    private static void addAlertScopePredicate(
            List<Predicate> predicates,
            Join<Alert, Shipment> shipmentJoin,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            AppUser user
    ) {
        if (user.getRole() == UserRole.MANUFACTURER) {
            predicates.add(hasScopedKey(
                    criteriaBuilder,
                    shipmentJoin.get("manufacturerKey"),
                    user.getPartyProfileKey()
            ));
        }

        if (user.getRole() == UserRole.DEALERSHIP) {
            predicates.add(hasScopedKey(
                    criteriaBuilder,
                    shipmentJoin.get("dealershipKey"),
                    user.getPartyProfileKey()
            ));
        }
    }

    private static Predicate hasScopedKey(
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            jakarta.persistence.criteria.Expression<String> path,
            String partyProfileKey
    ) {
        if (!StringUtils.hasText(partyProfileKey)) {
            return criteriaBuilder.disjunction();
        }

        return criteriaBuilder.equal(path, partyProfileKey);
    }

    private static void addDateRangePredicate(
            List<Predicate> predicates,
            jakarta.persistence.criteria.Path<LocalDateTime> path,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            LocalDateTime from,
            LocalDateTime to
    ) {
        if (from != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(path, from));
        }

        if (to != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(path, to));
        }
    }
}
