package com.fyp.autologistics.service;

import com.fyp.autologistics.dto.report.AlertReportResponse;
import com.fyp.autologistics.dto.report.AlertReportRowResponse;
import com.fyp.autologistics.dto.report.DelayReportResponse;
import com.fyp.autologistics.dto.report.DelayReportRowResponse;
import com.fyp.autologistics.dto.report.ReportMetricResponse;
import com.fyp.autologistics.dto.report.ReportOptionResponse;
import com.fyp.autologistics.dto.report.ReportOptionsResponse;
import com.fyp.autologistics.dto.report.ShipmentReportResponse;
import com.fyp.autologistics.dto.report.ShipmentReportRowResponse;
import com.fyp.autologistics.entity.Alert;
import com.fyp.autologistics.entity.AppUser;
import com.fyp.autologistics.entity.Shipment;
import com.fyp.autologistics.enums.AlertSeverity;
import com.fyp.autologistics.enums.AlertStatus;
import com.fyp.autologistics.enums.AlertType;
import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.enums.UserRole;
import com.fyp.autologistics.exception.BadRequestException;
import com.fyp.autologistics.repository.AlertRepository;
import com.fyp.autologistics.repository.ShipmentRepository;
import com.fyp.autologistics.specification.ReportSpecifications;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Service
public class ReportService {

    private static final Sort NEWEST_SHIPMENT_FIRST =
            Sort.by(Sort.Direction.DESC, "createdAt");
    private static final Sort NEWEST_ALERT_FIRST =
            Sort.by(Sort.Direction.DESC, "createdAt");
    private static final DateTimeFormatter CSV_DATE_TIME_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final Set<AlertType> DELAY_ALERT_TYPES = EnumSet.of(
            AlertType.DELAY,
            AlertType.IDLE,
            AlertType.ETA_OVERDUE,
            AlertType.ROUTE_DEVIATION,
            AlertType.PROGRESS_SHORTFALL,
            AlertType.MISSING_UPDATE
    );

    private final ShipmentRepository shipmentRepository;
    private final AlertRepository alertRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public ReportService(
            ShipmentRepository shipmentRepository,
            AlertRepository alertRepository,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.shipmentRepository = shipmentRepository;
        this.alertRepository = alertRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    public ReportOptionsResponse getReportOptions() {
        return new ReportOptionsResponse(
                toOptions(ShipmentStatus.values()),
                toOptions(LogisticsStage.values()),
                toOptions(AlertType.values()),
                toOptions(AlertStatus.values()),
                toOptions(AlertSeverity.values())
        );
    }

    @Transactional
    public ShipmentReportResponse getShipmentReport(
            String email,
            String search,
            ShipmentStatus status,
            LogisticsStage stage,
            LocalDateTime from,
            LocalDateTime to
    ) {
        validateDateRange(from, to);

        AppUser user = authenticatedUserService.getAuthenticatedUser(email);
        List<Shipment> shipments = findShipments(user, search, status, stage, from, to);
        List<ShipmentReportRowResponse> rows = shipments.stream()
                .map(this::mapShipmentReportRow)
                .toList();

        long totalShipments = rows.size();
        long deliveredShipments = countRows(rows, row -> row.status() == ShipmentStatus.DELIVERED);
        long delayedShipments = countRows(rows, row -> row.status() == ShipmentStatus.DELAYED);
        long cancelledShipments = countRows(rows, row -> row.status() == ShipmentStatus.CANCELLED);
        long activeShipments = totalShipments - deliveredShipments - cancelledShipments;

        return new ShipmentReportResponse(
                LocalDateTime.now(),
                resolveScopeLabel(user),
                totalShipments,
                activeShipments,
                delayedShipments,
                deliveredShipments,
                cancelledShipments,
                calculateRate(deliveredShipments, totalShipments),
                calculateRate(delayedShipments, totalShipments),
                buildBreakdown(ShipmentStatus.values(), rows, ShipmentReportRowResponse::status),
                buildBreakdown(LogisticsStage.values(), rows, ShipmentReportRowResponse::currentStage),
                rows
        );
    }

    @Transactional
    public DelayReportResponse getDelayReport(
            String email,
            String search,
            AlertStatus status,
            AlertSeverity severity,
            LocalDateTime from,
            LocalDateTime to
    ) {
        validateDateRange(from, to);

        AppUser user = authenticatedUserService.getAuthenticatedUser(email);
        List<Alert> delayAlerts = findAlerts(
                user,
                search,
                null,
                status,
                severity,
                DELAY_ALERT_TYPES,
                from,
                to
        );
        List<Long> alertShipmentIds = delayAlerts.stream()
                .map(alert -> alert.getShipment().getId())
                .distinct()
                .toList();
        List<DelayReportRowResponse> alertRows = delayAlerts.stream()
                .map(this::mapDelayAlertRow)
                .toList();
        List<DelayReportRowResponse> shipmentRows = findShipments(
                user,
                search,
                ShipmentStatus.DELAYED,
                null,
                from,
                to
        )
                .stream()
                .filter(shipment -> !alertShipmentIds.contains(shipment.getId()))
                .map(this::mapDelayedShipmentRow)
                .toList();
        List<DelayReportRowResponse> rows = concatDelayRows(alertRows, shipmentRows);

        return new DelayReportResponse(
                LocalDateTime.now(),
                resolveScopeLabel(user),
                rows.size(),
                countRows(rows, row -> row.alertStatus() == AlertStatus.OPEN),
                countRows(rows, row -> row.alertStatus() == AlertStatus.RESOLVED),
                countRows(rows, row -> row.severity() == AlertSeverity.HIGH),
                buildBreakdown(AlertType.values(), rows, DelayReportRowResponse::alertType),
                buildBreakdown(AlertSeverity.values(), rows, DelayReportRowResponse::severity),
                rows
        );
    }

    @Transactional
    public AlertReportResponse getAlertReport(
            String email,
            String search,
            AlertType alertType,
            AlertStatus status,
            AlertSeverity severity,
            LocalDateTime from,
            LocalDateTime to
    ) {
        validateDateRange(from, to);

        AppUser user = authenticatedUserService.getAuthenticatedUser(email);
        List<Alert> alerts = findAlerts(user, search, alertType, status, severity, null, from, to);
        List<AlertReportRowResponse> rows = alerts.stream()
                .map(this::mapAlertReportRow)
                .toList();

        return new AlertReportResponse(
                LocalDateTime.now(),
                resolveScopeLabel(user),
                rows.size(),
                countRows(rows, row -> row.status() == AlertStatus.OPEN),
                countRows(rows, row -> row.status() == AlertStatus.ACKNOWLEDGED),
                countRows(rows, row -> row.status() == AlertStatus.RESOLVED),
                countRows(rows, row -> row.severity() == AlertSeverity.HIGH),
                buildBreakdown(AlertType.values(), rows, AlertReportRowResponse::alertType),
                buildBreakdown(AlertSeverity.values(), rows, AlertReportRowResponse::severity),
                buildBreakdown(AlertStatus.values(), rows, AlertReportRowResponse::status),
                rows
        );
    }

    @Transactional
    public String exportShipmentReportCsv(
            String email,
            String search,
            ShipmentStatus status,
            LogisticsStage stage,
            LocalDateTime from,
            LocalDateTime to
    ) {
        ShipmentReportResponse report = getShipmentReport(email, search, status, stage, from, to);
        StringBuilder csv = new StringBuilder();
        appendCsvRow(csv, "Shipment Code", "VIN", "Model", "Origin", "Destination",
                "Dealership", "Status", "Stage", "ETD", "ETA", "Created", "Updated",
                "Receipt Confirmed");

        report.shipments().forEach(row -> appendCsvRow(
                csv,
                row.shipmentCode(),
                row.vehicleVin(),
                row.vehicleModel(),
                row.origin(),
                row.destination(),
                row.dealershipName(),
                enumName(row.status()),
                enumName(row.currentStage()),
                formatDateTime(row.estimatedDeparture()),
                formatDateTime(row.estimatedArrival()),
                formatDateTime(row.createdAt()),
                formatDateTime(row.updatedAt()),
                formatDateTime(row.receiptConfirmedAt())
        ));

        return csv.toString();
    }

    @Transactional
    public String exportDelayReportCsv(
            String email,
            String search,
            AlertStatus status,
            AlertSeverity severity,
            LocalDateTime from,
            LocalDateTime to
    ) {
        DelayReportResponse report = getDelayReport(email, search, status, severity, from, to);
        StringBuilder csv = new StringBuilder();
        appendCsvRow(csv, "Shipment Code", "VIN", "Origin", "Destination", "Shipment Status",
                "Stage", "Alert Type", "Severity", "Alert Status", "Reason", "Detected At",
                "ETD", "ETA");

        report.delays().forEach(row -> appendCsvRow(
                csv,
                row.shipmentCode(),
                row.vehicleVin(),
                row.origin(),
                row.destination(),
                enumName(row.shipmentStatus()),
                enumName(row.currentStage()),
                enumName(row.alertType()),
                enumName(row.severity()),
                enumName(row.alertStatus()),
                row.reason(),
                formatDateTime(row.detectedAt()),
                formatDateTime(row.estimatedDeparture()),
                formatDateTime(row.estimatedArrival())
        ));

        return csv.toString();
    }

    @Transactional
    public String exportAlertReportCsv(
            String email,
            String search,
            AlertType alertType,
            AlertStatus status,
            AlertSeverity severity,
            LocalDateTime from,
            LocalDateTime to
    ) {
        AlertReportResponse report =
                getAlertReport(email, search, alertType, status, severity, from, to);
        StringBuilder csv = new StringBuilder();
        appendCsvRow(csv, "Alert ID", "Shipment Code", "Type", "Severity", "Status", "Title",
                "Message", "Created", "Acknowledged", "Resolved");

        report.alerts().forEach(row -> appendCsvRow(
                csv,
                row.alertId(),
                row.shipmentCode(),
                enumName(row.alertType()),
                enumName(row.severity()),
                enumName(row.status()),
                row.title(),
                row.message(),
                formatDateTime(row.createdAt()),
                formatDateTime(row.acknowledgedAt()),
                formatDateTime(row.resolvedAt())
        ));

        return csv.toString();
    }

    private List<Shipment> findShipments(
            AppUser user,
            String search,
            ShipmentStatus status,
            LogisticsStage stage,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return shipmentRepository.findAll(
                ReportSpecifications.shipmentReport(user, search, status, stage, from, to),
                NEWEST_SHIPMENT_FIRST
        );
    }

    private List<Alert> findAlerts(
            AppUser user,
            String search,
            AlertType alertType,
            AlertStatus status,
            AlertSeverity severity,
            Set<AlertType> allowedAlertTypes,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return alertRepository.findAll(
                ReportSpecifications.alertReport(
                        user,
                        search,
                        alertType,
                        status,
                        severity,
                        allowedAlertTypes,
                        from,
                        to
                ),
                NEWEST_ALERT_FIRST
        );
    }

    private ShipmentReportRowResponse mapShipmentReportRow(Shipment shipment) {
        return new ShipmentReportRowResponse(
                shipment.getId(),
                shipment.getShipmentCode(),
                shipment.getVehicleVin(),
                shipment.getVehicleModel(),
                shipment.getOrigin(),
                shipment.getDestination(),
                shipment.getDealershipName(),
                shipment.getStatus(),
                shipment.getCurrentStage(),
                shipment.getEstimatedDeparture(),
                shipment.getEstimatedArrival(),
                shipment.getCreatedAt(),
                shipment.getUpdatedAt(),
                shipment.getReceiptConfirmedAt()
        );
    }

    private DelayReportRowResponse mapDelayAlertRow(Alert alert) {
        Shipment shipment = alert.getShipment();

        return new DelayReportRowResponse(
                shipment.getId(),
                shipment.getShipmentCode(),
                shipment.getVehicleVin(),
                shipment.getOrigin(),
                shipment.getDestination(),
                shipment.getStatus(),
                shipment.getCurrentStage(),
                shipment.getEstimatedDeparture(),
                shipment.getEstimatedArrival(),
                alert.getAlertType(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getTitle(),
                alert.getCreatedAt()
        );
    }

    private DelayReportRowResponse mapDelayedShipmentRow(Shipment shipment) {
        return new DelayReportRowResponse(
                shipment.getId(),
                shipment.getShipmentCode(),
                shipment.getVehicleVin(),
                shipment.getOrigin(),
                shipment.getDestination(),
                shipment.getStatus(),
                shipment.getCurrentStage(),
                shipment.getEstimatedDeparture(),
                shipment.getEstimatedArrival(),
                null,
                null,
                null,
                "Shipment is currently marked as delayed.",
                shipment.getUpdatedAt()
        );
    }

    private AlertReportRowResponse mapAlertReportRow(Alert alert) {
        return new AlertReportRowResponse(
                alert.getId(),
                alert.getShipment().getId(),
                alert.getShipment().getShipmentCode(),
                alert.getAlertType(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getTitle(),
                alert.getMessage(),
                alert.getCreatedAt(),
                alert.getAcknowledgedAt(),
                alert.getResolvedAt()
        );
    }

    private List<DelayReportRowResponse> concatDelayRows(
            List<DelayReportRowResponse> alertRows,
            List<DelayReportRowResponse> shipmentRows
    ) {
        return java.util.stream.Stream.concat(alertRows.stream(), shipmentRows.stream())
                .sorted((left, right) -> {
                    LocalDateTime leftDate = left.detectedAt();
                    LocalDateTime rightDate = right.detectedAt();

                    if (leftDate == null && rightDate == null) {
                        return 0;
                    }

                    if (leftDate == null) {
                        return 1;
                    }

                    if (rightDate == null) {
                        return -1;
                    }

                    return rightDate.compareTo(leftDate);
                })
                .toList();
    }

    private <E extends Enum<E>> List<ReportOptionResponse> toOptions(E[] values) {
        return Arrays.stream(values)
                .map(value -> new ReportOptionResponse(value.name(), formatEnumLabel(value.name())))
                .toList();
    }

    private <E extends Enum<E>, T> List<ReportMetricResponse> buildBreakdown(
            E[] values,
            List<T> rows,
            Function<T, E> extractor
    ) {
        EnumMap<E, Long> counts = new EnumMap<>(values[0].getDeclaringClass());

        rows.forEach(row -> {
            E key = extractor.apply(row);
            if (key != null) {
                counts.merge(key, 1L, Long::sum);
            }
        });

        return Arrays.stream(values)
                .map(value -> new ReportMetricResponse(
                        value.name(),
                        formatEnumLabel(value.name()),
                        counts.getOrDefault(value, 0L)
                ))
                .toList();
    }

    private <T> long countRows(List<T> rows, java.util.function.Predicate<T> predicate) {
        return rows.stream().filter(predicate).count();
    }

    private double calculateRate(long numerator, long denominator) {
        if (denominator == 0) {
            return 0.0;
        }

        return Math.round(((double) numerator / denominator) * 1000.0) / 10.0;
    }

    private void validateDateRange(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("Report start date must be before the end date.");
        }
    }

    private String resolveScopeLabel(AppUser user) {
        if (user.getRole() == UserRole.ADMIN) {
            return "All system records";
        }

        if (user.getRole() == UserRole.LOGISTICS_PROVIDER) {
            return "Shared transport operations";
        }

        if (StringUtils.hasText(user.getCompanyName())) {
            return user.getCompanyName();
        }

        return formatEnumLabel(user.getRole().name());
    }

    private String formatEnumLabel(String value) {
        return Arrays.stream(value.toLowerCase().split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .reduce((left, right) -> left + " " + right)
                .orElse(value);
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : CSV_DATE_TIME_FORMATTER.format(value);
    }

    private String enumName(Enum<?> value) {
        return value == null ? "" : value.name();
    }

    private void appendCsvRow(StringBuilder csv, Object... values) {
        for (int index = 0; index < values.length; index++) {
            if (index > 0) {
                csv.append(',');
            }

            csv.append(escapeCsv(values[index]));
        }

        csv.append(System.lineSeparator());
    }

    private String escapeCsv(Object value) {
        if (value == null) {
            return "";
        }

        String text = String.valueOf(value);
        boolean requiresQuotes =
                text.contains(",") || text.contains("\"") || text.contains("\n")
                        || text.contains("\r");

        if (!requiresQuotes) {
            return text;
        }

        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
