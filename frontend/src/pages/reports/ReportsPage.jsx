import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { reportApi } from "../../api/reportApi";
import SummaryCard from "../../components/dashboard/SummaryCard";
import MetricBarChart from "../../components/reports/MetricBarChart";
import { formatDateTime, formatEnumLabel } from "../../utils/formatters";

const reportTabs = [
  {
    key: "shipments",
    label: "Shipment Report",
    csvName: "shipment-report.csv",
  },
  {
    key: "delays",
    label: "Delay Report",
    csvName: "delay-report.csv",
  },
  {
    key: "alerts",
    label: "Alert Report",
    csvName: "alert-report.csv",
  },
];

const emptyFilters = {
  search: "",
  from: "",
  to: "",
  shipmentStatus: "",
  stage: "",
  alertType: "",
  alertStatus: "",
  severity: "",
};

function toApiDateTime(value) {
  if (!value) {
    return "";
  }

  return value.length === 16 ? `${value}:00` : value;
}

function buildReportParams(reportKey, filters) {
  const params = {
    search: filters.search,
    from: toApiDateTime(filters.from),
    to: toApiDateTime(filters.to),
  };

  if (reportKey === "shipments") {
    return {
      ...params,
      status: filters.shipmentStatus,
      stage: filters.stage,
    };
  }

  if (reportKey === "delays") {
    return {
      ...params,
      status: filters.alertStatus,
      severity: filters.severity,
    };
  }

  return {
    ...params,
    alertType: filters.alertType,
    status: filters.alertStatus,
    severity: filters.severity,
  };
}

function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement("a");

  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}

function formatPercent(value) {
  return `${Number(value ?? 0).toFixed(1)}%`;
}

function ReportsPage() {
  const [activeReport, setActiveReport] = useState("shipments");
  const [filters, setFilters] = useState(emptyFilters);
  const [options, setOptions] = useState({
    shipmentStatuses: [],
    logisticsStages: [],
    alertTypes: [],
    alertStatuses: [],
    alertSeverities: [],
  });
  const [reportData, setReportData] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isExporting, setIsExporting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const activeTab = useMemo(
    () => reportTabs.find((tab) => tab.key === activeReport) ?? reportTabs[0],
    [activeReport],
  );

  const fetchReport = useCallback(async (reportKey, selectedFilters) => {
    const params = buildReportParams(reportKey, selectedFilters);

    if (reportKey === "shipments") {
      return reportApi.getShipmentReport(params);
    }

    if (reportKey === "delays") {
      return reportApi.getDelayReport(params);
    }

    return reportApi.getAlertReport(params);
  }, []);

  const loadReport = useCallback(
    async (reportKey = activeReport, selectedFilters = filters) => {
      try {
        setIsLoading(true);
        setErrorMessage("");

        const data = await fetchReport(reportKey, selectedFilters);
        setReportData(data);
      } catch {
        setErrorMessage("Failed to load report data.");
      } finally {
        setIsLoading(false);
      }
    },
    [activeReport, fetchReport, filters],
  );

  useEffect(() => {
    let isActive = true;

    async function loadInitialReport() {
      try {
        const [optionsData, reportResponse] = await Promise.all([
          reportApi.getOptions(),
          fetchReport("shipments", emptyFilters),
        ]);

        if (!isActive) {
          return;
        }

        setOptions(optionsData);
        setReportData(reportResponse);
      } catch {
        if (isActive) {
          setErrorMessage("Failed to load report data.");
        }
      } finally {
        if (isActive) {
          setIsLoading(false);
        }
      }
    }

    loadInitialReport();

    return () => {
      isActive = false;
    };
  }, [fetchReport]);

  const handleFilterChange = (event) => {
    const { name, value } = event.target;

    setFilters((currentFilters) => ({
      ...currentFilters,
      [name]: value,
    }));
  };

  const handleTabChange = async (reportKey) => {
    setActiveReport(reportKey);
    await loadReport(reportKey, filters);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    await loadReport(activeReport, filters);
  };

  const handleReset = async () => {
    setFilters(emptyFilters);
    await loadReport(activeReport, emptyFilters);
  };

  const handleExport = async () => {
    try {
      setIsExporting(true);
      const params = buildReportParams(activeReport, filters);
      const blob = await downloadActiveReportCsv(activeReport, params);

      downloadBlob(blob, activeTab.csvName);
    } finally {
      setIsExporting(false);
    }
  };

  return (
    <div className="page reports-page">
      <div className="page-header">
        <div>
          <p>Operational reporting</p>
          <h2>Reports</h2>
          <p>{reportData?.scopeLabel ?? "Loading report scope"}</p>
        </div>

        <button type="button" onClick={handleExport} disabled={isExporting || !reportData}>
          {isExporting ? "Exporting..." : "Export CSV"}
        </button>
      </div>

      <div className="report-tabs" role="tablist" aria-label="Report type">
        {reportTabs.map((tab) => (
          <button
            aria-selected={activeReport === tab.key}
            className={activeReport === tab.key ? "active" : ""}
            key={tab.key}
            onClick={() => handleTabChange(tab.key)}
            role="tab"
            type="button"
          >
            {tab.label}
          </button>
        ))}
      </div>

      <form className="report-filter-panel panel" onSubmit={handleSubmit}>
        <label>
          <span>Search</span>
          <input
            name="search"
            value={filters.search}
            onChange={handleFilterChange}
            placeholder="Shipment code, VIN, alert text"
          />
        </label>

        <label>
          <span>From</span>
          <input
            name="from"
            type="datetime-local"
            value={filters.from}
            onChange={handleFilterChange}
          />
        </label>

        <label>
          <span>To</span>
          <input
            name="to"
            type="datetime-local"
            value={filters.to}
            onChange={handleFilterChange}
          />
        </label>

        {activeReport === "shipments" && (
          <>
            <ReportSelect
              label="Shipment Status"
              name="shipmentStatus"
              options={options.shipmentStatuses}
              value={filters.shipmentStatus}
              onChange={handleFilterChange}
            />
            <ReportSelect
              label="Logistics Stage"
              name="stage"
              options={options.logisticsStages}
              value={filters.stage}
              onChange={handleFilterChange}
            />
          </>
        )}

        {activeReport === "alerts" && (
          <ReportSelect
            label="Alert Type"
            name="alertType"
            options={options.alertTypes}
            value={filters.alertType}
            onChange={handleFilterChange}
          />
        )}

        {activeReport !== "shipments" && (
          <>
            <ReportSelect
              label="Alert Status"
              name="alertStatus"
              options={options.alertStatuses}
              value={filters.alertStatus}
              onChange={handleFilterChange}
            />
            <ReportSelect
              label="Severity"
              name="severity"
              options={options.alertSeverities}
              value={filters.severity}
              onChange={handleFilterChange}
            />
          </>
        )}

        <div className="report-filter-actions">
          <button type="submit">Apply</button>
          <button className="secondary-button" type="button" onClick={handleReset}>
            Reset
          </button>
        </div>
      </form>

      {isLoading && <p>Loading report...</p>}
      {errorMessage && <p className="error-message">{errorMessage}</p>}

      {!isLoading && !errorMessage && activeReport === "shipments" && (
        <ShipmentReportView report={reportData} />
      )}

      {!isLoading && !errorMessage && activeReport === "delays" && (
        <DelayReportView report={reportData} />
      )}

      {!isLoading && !errorMessage && activeReport === "alerts" && (
        <AlertReportView report={reportData} />
      )}
    </div>
  );
}

function ReportSelect({ label, name, options, value, onChange }) {
  return (
    <label>
      <span>{label}</span>
      <select name={name} value={value} onChange={onChange}>
        <option value="">All</option>
        {options.map((option) => (
          <option key={option.key} value={option.key}>
            {option.label}
          </option>
        ))}
      </select>
    </label>
  );
}

function ShipmentReportView({ report }) {
  return (
    <>
      <div className="summary-grid reports-summary-grid">
        <SummaryCard
          title="Total"
          value={report.totalShipments}
          description="Shipments matching current filters"
        />
        <SummaryCard
          title="Active"
          value={report.activeShipments}
          description="Not delivered or cancelled"
        />
        <SummaryCard
          title="Delayed"
          value={report.delayedShipments}
          description={`${formatPercent(report.delayRate)} delay rate`}
        />
        <SummaryCard
          title="Delivered"
          value={report.deliveredShipments}
          description={`${formatPercent(report.deliveryCompletionRate)} completion rate`}
        />
        <SummaryCard
          title="Cancelled"
          value={report.cancelledShipments}
          description="Stopped shipment records"
        />
      </div>

      <div className="report-chart-grid">
        <MetricBarChart title="Shipment Status" metrics={report.statusBreakdown} />
        <MetricBarChart title="Logistics Stage" metrics={report.stageBreakdown} />
      </div>

      <ReportTable emptyMessage="No shipments match the current filters.">
        <thead>
          <tr>
            <th>Shipment</th>
            <th>VIN</th>
            <th>Status</th>
            <th>Stage</th>
            <th>Route</th>
            <th>ETA</th>
            <th>Updated</th>
          </tr>
        </thead>
        <tbody>
          {report.shipments.map((shipment) => (
            <tr key={shipment.shipmentId}>
              <td>
                <Link to={`/shipments/${shipment.shipmentId}`}>
                  {shipment.shipmentCode}
                </Link>
                <span>{shipment.vehicleModel}</span>
              </td>
              <td>{shipment.vehicleVin}</td>
              <td>{formatEnumLabel(shipment.status)}</td>
              <td>{formatEnumLabel(shipment.currentStage)}</td>
              <td>
                {shipment.origin} to {shipment.destination}
              </td>
              <td>{formatDateTime(shipment.estimatedArrival)}</td>
              <td>{formatDateTime(shipment.updatedAt)}</td>
            </tr>
          ))}
        </tbody>
      </ReportTable>
    </>
  );
}

function DelayReportView({ report }) {
  return (
    <>
      <div className="summary-grid reports-summary-grid">
        <SummaryCard
          title="Delay Records"
          value={report.totalDelayRecords}
          description="Delay and exception records"
        />
        <SummaryCard
          title="Open"
          value={report.openDelayAlerts}
          description="Still requiring attention"
        />
        <SummaryCard
          title="Resolved"
          value={report.resolvedDelayAlerts}
          description="Closed delay alerts"
        />
        <SummaryCard
          title="High Severity"
          value={report.highSeverityDelayAlerts}
          description="Highest operational risk"
        />
      </div>

      <div className="report-chart-grid">
        <MetricBarChart title="Delay Type" metrics={report.delayTypeBreakdown} />
        <MetricBarChart title="Severity" metrics={report.severityBreakdown} />
      </div>

      <ReportTable emptyMessage="No delay records match the current filters.">
        <thead>
          <tr>
            <th>Shipment</th>
            <th>Status</th>
            <th>Reason</th>
            <th>Severity</th>
            <th>Alert Status</th>
            <th>Detected</th>
            <th>ETA</th>
          </tr>
        </thead>
        <tbody>
          {report.delays.map((delay) => (
            <tr key={`${delay.shipmentId}-${delay.detectedAt}-${delay.reason}`}>
              <td>
                <Link to={`/shipments/${delay.shipmentId}`}>
                  {delay.shipmentCode}
                </Link>
                <span>{delay.vehicleVin}</span>
              </td>
              <td>{formatEnumLabel(delay.shipmentStatus)}</td>
              <td>{delay.reason}</td>
              <td>{formatEnumLabel(delay.severity)}</td>
              <td>{formatEnumLabel(delay.alertStatus)}</td>
              <td>{formatDateTime(delay.detectedAt)}</td>
              <td>{formatDateTime(delay.estimatedArrival)}</td>
            </tr>
          ))}
        </tbody>
      </ReportTable>
    </>
  );
}

function AlertReportView({ report }) {
  return (
    <>
      <div className="summary-grid reports-summary-grid">
        <SummaryCard
          title="Total Alerts"
          value={report.totalAlerts}
          description="Alerts matching current filters"
        />
        <SummaryCard
          title="Open"
          value={report.openAlerts}
          description="Awaiting action"
        />
        <SummaryCard
          title="Acknowledged"
          value={report.acknowledgedAlerts}
          description="Seen by operations"
        />
        <SummaryCard
          title="Resolved"
          value={report.resolvedAlerts}
          description="Closed alerts"
        />
        <SummaryCard
          title="High Severity"
          value={report.highSeverityAlerts}
          description="Highest alert priority"
        />
      </div>

      <div className="report-chart-grid">
        <MetricBarChart title="Alert Type" metrics={report.typeBreakdown} />
        <MetricBarChart title="Severity" metrics={report.severityBreakdown} />
        <MetricBarChart title="Alert Status" metrics={report.statusBreakdown} />
      </div>

      <ReportTable emptyMessage="No alerts match the current filters.">
        <thead>
          <tr>
            <th>Shipment</th>
            <th>Type</th>
            <th>Severity</th>
            <th>Status</th>
            <th>Alert</th>
            <th>Created</th>
            <th>Resolved</th>
          </tr>
        </thead>
        <tbody>
          {report.alerts.map((alert) => (
            <tr key={alert.alertId}>
              <td>
                <Link to={`/shipments/${alert.shipmentId}`}>
                  {alert.shipmentCode}
                </Link>
              </td>
              <td>{formatEnumLabel(alert.alertType)}</td>
              <td>{formatEnumLabel(alert.severity)}</td>
              <td>{formatEnumLabel(alert.status)}</td>
              <td>
                {alert.title}
                <span>{alert.message}</span>
              </td>
              <td>{formatDateTime(alert.createdAt)}</td>
              <td>{formatDateTime(alert.resolvedAt)}</td>
            </tr>
          ))}
        </tbody>
      </ReportTable>
    </>
  );
}

async function downloadActiveReportCsv(reportKey, params) {
  if (reportKey === "shipments") {
    return reportApi.downloadShipmentCsv(params);
  }

  if (reportKey === "delays") {
    return reportApi.downloadDelayCsv(params);
  }

  return reportApi.downloadAlertCsv(params);
}

function ReportTable({ children, emptyMessage }) {
  const tableBody = Array.isArray(children) ? children[1] : null;
  const rows = tableBody?.props?.children ?? [];
  const rowCount = Array.isArray(rows) ? rows.length : 1;

  return (
    <div className="table-card report-table-card">
      {rowCount === 0 ? (
        <p>{emptyMessage}</p>
      ) : (
        <table className="data-table report-table">{children}</table>
      )}
    </div>
  );
}

export default ReportsPage;
