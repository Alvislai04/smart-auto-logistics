import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { dashboardApi } from "../api/dashboardApi";
import { useAuth } from "../auth/AuthContext";
import SummaryCard from "../components/dashboard/SummaryCard";
import OpenAlertsPanel from "../components/dashboard/OpenAlertsPanel";
import {
  dashboardRefreshIntervalMs,
  dashboardRefreshIntervalSeconds,
} from "../config/dashboardConfig";
import { formatEnumLabel } from "../utils/formatters";

const dashboardViews = {
  ADMIN: {
    title: "Admin dashboard",
    subtitle:
      "System-wide overview for shipment activity, operational alerts, and platform coordination.",
    scopeDescription: "All registered shipments",
    alertTitle: "System Open Alerts",
    emptyAlertMessage: "No open system alerts at the moment.",
  },
  MANUFACTURER: {
    title: "Manufacturer dashboard",
    subtitle:
      "Dispatch-focused overview for shipments created from your registered manufacturer profile.",
    scopeDescription: "Your manufacturer shipments",
    alertTitle: "Manufacturer Shipment Alerts",
    emptyAlertMessage: "No open alerts for your manufacturer shipments.",
  },
  LOGISTICS_PROVIDER: {
    title: "Logistics provider dashboard",
    subtitle:
      "Transport operations overview for shipment movement, delays, and route monitoring.",
    scopeDescription: "Transport operations shipments",
    alertTitle: "Transport Operation Alerts",
    emptyAlertMessage: "No open transport operation alerts at the moment.",
  },
  DEALERSHIP: {
    title: "Dealership dashboard",
    subtitle:
      "Inbound delivery overview for shipments assigned to your registered dealership profile.",
    scopeDescription: "Your incoming dealership shipments",
    alertTitle: "Dealership Delivery Alerts",
    emptyAlertMessage: "No open alerts for your dealership deliveries.",
  },
};

function resolveDashboardView(role) {
  return (
    dashboardViews[role] ?? {
      title: "Dashboard",
      subtitle: "Shipment overview for your account.",
      scopeDescription: "Shipments visible to your account",
      alertTitle: "Open Alerts",
      emptyAlertMessage: "No open alerts at the moment.",
    }
  );
}

function DashboardPage() {
  const { currentUser } = useAuth();
  const [summary, setSummary] = useState(null);
  const [alerts, setAlerts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [lastUpdatedAt, setLastUpdatedAt] = useState(null);
  const [errorMessage, setErrorMessage] = useState("");
  const [refreshErrorMessage, setRefreshErrorMessage] = useState("");
  const isRequestInFlightRef = useRef(false);
  const hasDashboardDataRef = useRef(false);
  const dashboardView = resolveDashboardView(currentUser?.role);

  const fetchDashboardData = useCallback(async (signal) => {
    const overview = await dashboardApi.getOverview({ signal });

    return {
      summaryData: overview.summary,
      openAlerts: overview.openAlerts ?? [],
    };
  }, []);

  const loadDashboardData = useCallback(async ({
    showLoading = false,
    signal,
  } = {}) => {
    if (isRequestInFlightRef.current) {
      return;
    }

    try {
      isRequestInFlightRef.current = true;

      if (showLoading) {
        setIsLoading(true);
      } else {
        setIsRefreshing(true);
      }

      setErrorMessage("");
      setRefreshErrorMessage("");

      const { summaryData, openAlerts } = await fetchDashboardData(signal);

      setSummary(summaryData);
      setAlerts(openAlerts);
      setLastUpdatedAt(new Date().toISOString());
      hasDashboardDataRef.current = true;
    } catch (error) {
      if (error.name === "CanceledError" || error.code === "ERR_CANCELED") {
        return;
      }

      if (hasDashboardDataRef.current) {
        setRefreshErrorMessage("Live dashboard refresh failed. Showing latest loaded data.");
      } else {
        setErrorMessage("Failed to load dashboard data.");
      }
    } finally {
      isRequestInFlightRef.current = false;

      if (!signal?.aborted) {
        if (showLoading) {
          setIsLoading(false);
        } else {
          setIsRefreshing(false);
        }
      }
    }
  }, [fetchDashboardData]);

  useEffect(() => {
    const controller = new AbortController();
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadDashboardData({ showLoading: true, signal: controller.signal });

    return () => {
      controller.abort();
    };
  }, [loadDashboardData]);

  useEffect(() => {
    const refreshDashboard = () => {
      if (document.visibilityState !== "visible") {
        return;
      }

      loadDashboardData();
    };

    const refreshIntervalId = window.setInterval(
      refreshDashboard,
      dashboardRefreshIntervalMs,
    );

    const handleVisibilityChange = () => {
      if (document.visibilityState === "visible") {
        loadDashboardData();
      }
    };

    document.addEventListener("visibilitychange", handleVisibilityChange);

    return () => {
      window.clearInterval(refreshIntervalId);
      document.removeEventListener("visibilitychange", handleVisibilityChange);
    };
  }, [loadDashboardData]);

  const summaryCards = useMemo(
    () => [
      {
        title: "Total Shipments",
        value: summary?.totalShipments ?? 0,
        description: dashboardView.scopeDescription,
      },
      {
        title: "Created",
        value: summary?.createdShipments ?? 0,
        description: "Registered and pending dispatch preparation",
      },
      {
        title: "Ready",
        value: summary?.readyForDispatchShipments ?? 0,
        description: "Prepared for pickup and dispatch",
      },
      {
        title: "Picked Up",
        value: summary?.pickedUpShipments ?? 0,
        description: "Collected by transport operations",
      },
      {
        title: "In Transit",
        value: summary?.inTransitShipments ?? 0,
        description: "Currently moving vehicles",
      },
      {
        title: "Delayed",
        value: summary?.delayedShipments ?? 0,
        description: "Shipments requiring attention",
      },
      {
        title: "Arrived",
        value: summary?.arrivedShipments ?? 0,
        description: "Reached dealership and awaiting receipt",
      },
      {
        title: "Delivered",
        value: summary?.deliveredShipments ?? 0,
        description: "Completed shipment records",
      },
      {
        title: "Cancelled",
        value: summary?.cancelledShipments ?? 0,
        description: "Stopped shipment records",
      },
      {
        title: "Open Alerts",
        value: summary?.openAlerts ?? 0,
        description: "Unresolved alerts in this dashboard scope",
      },
    ],
    [dashboardView.scopeDescription, summary],
  );

  if (isLoading) {
    return <p>Loading dashboard...</p>;
  }

  if (errorMessage) {
    return <p className="error-message">{errorMessage}</p>;
  }

  const lastUpdatedLabel = lastUpdatedAt
    ? new Date(lastUpdatedAt).toLocaleTimeString("en-MY", {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
      })
    : "-";

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <p>{formatEnumLabel(currentUser?.role)} workspace</p>
          <h2>{dashboardView.title}</h2>
          <p>{dashboardView.subtitle}</p>
        </div>

        <div className="dashboard-live-actions">
          <div className="dashboard-live-status" aria-live="polite">
            <span
              className={`dashboard-live-dot ${isRefreshing ? "refreshing" : ""}`}
            />
            <div>
              <strong>{isRefreshing ? "Refreshing" : "Live dashboard"}</strong>
              <span>
                Every {dashboardRefreshIntervalSeconds}s - Last {lastUpdatedLabel}
              </span>
            </div>
          </div>

          <button
            type="button"
            disabled={isRefreshing}
            onClick={() => loadDashboardData()}
          >
            {isRefreshing ? "Refreshing..." : "Refresh"}
          </button>
        </div>
      </div>

      {refreshErrorMessage && (
        <p className="dashboard-refresh-warning">{refreshErrorMessage}</p>
      )}

      <div className="summary-grid">
        {summaryCards.map((card) => (
          <SummaryCard
            key={card.title}
            title={card.title}
            value={card.value}
            description={card.description}
          />
        ))}
      </div>

      <OpenAlertsPanel
        alerts={alerts}
        title={dashboardView.alertTitle}
        emptyMessage={dashboardView.emptyAlertMessage}
      />
    </div>
  );
}

export default DashboardPage;
