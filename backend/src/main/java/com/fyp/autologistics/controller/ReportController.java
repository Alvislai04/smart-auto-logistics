package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.report.AlertReportResponse;
import com.fyp.autologistics.dto.report.DelayReportResponse;
import com.fyp.autologistics.dto.report.ReportOptionsResponse;
import com.fyp.autologistics.dto.report.ShipmentReportResponse;
import com.fyp.autologistics.enums.AlertSeverity;
import com.fyp.autologistics.enums.AlertStatus;
import com.fyp.autologistics.enums.AlertType;
import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.ShipmentStatus;
import com.fyp.autologistics.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:5173")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/options")
    public ReportOptionsResponse getReportOptions() {
        return reportService.getReportOptions();
    }

    @GetMapping("/shipments")
    public ShipmentReportResponse getShipmentReport(
            Authentication authentication,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(required = false) LogisticsStage stage,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to
    ) {
        return reportService.getShipmentReport(
                authentication.getName(),
                search,
                status,
                stage,
                from,
                to
        );
    }

    @GetMapping("/delays")
    public DelayReportResponse getDelayReport(
            Authentication authentication,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to
    ) {
        return reportService.getDelayReport(
                authentication.getName(),
                search,
                status,
                severity,
                from,
                to
        );
    }

    @GetMapping("/alerts")
    public AlertReportResponse getAlertReport(
            Authentication authentication,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AlertType alertType,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to
    ) {
        return reportService.getAlertReport(
                authentication.getName(),
                search,
                alertType,
                status,
                severity,
                from,
                to
        );
    }

    @GetMapping("/shipments.csv")
    public ResponseEntity<String> exportShipmentReportCsv(
            Authentication authentication,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(required = false) LogisticsStage stage,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to
    ) {
        String csv = reportService.exportShipmentReportCsv(
                authentication.getName(),
                search,
                status,
                stage,
                from,
                to
        );

        return csvResponse("shipment-report.csv", csv);
    }

    @GetMapping("/delays.csv")
    public ResponseEntity<String> exportDelayReportCsv(
            Authentication authentication,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to
    ) {
        String csv = reportService.exportDelayReportCsv(
                authentication.getName(),
                search,
                status,
                severity,
                from,
                to
        );

        return csvResponse("delay-report.csv", csv);
    }

    @GetMapping("/alerts.csv")
    public ResponseEntity<String> exportAlertReportCsv(
            Authentication authentication,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AlertType alertType,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to
    ) {
        String csv = reportService.exportAlertReportCsv(
                authentication.getName(),
                search,
                alertType,
                status,
                severity,
                from,
                to
        );

        return csvResponse("alert-report.csv", csv);
    }

    private ResponseEntity<String> csvResponse(String filename, String csv) {
        MediaType csvMediaType = new MediaType("text", "csv", StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString()
                )
                .contentType(csvMediaType)
                .body(csv);
    }
}
