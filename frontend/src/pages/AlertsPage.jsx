import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { alertApi } from "../api/alertApi";
import { useAuth } from "../auth/AuthContext";
import { useToast } from "../components/feedback/ToastContext";
import { alertCountRefreshEventName } from "../config/alertConfig";
import { formatDateTime, formatEnumLabel } from "../utils/formatters";

const ALERT_RESOLVE_ROLES = ["ADMIN", "LOGISTICS_PROVIDER"];
const MANUFACTURER_ACKNOWLEDGE_TYPES = ["LOW_STOCK", "ORDER_UPDATE"];

function AlertsPage() {
  const { currentUser } = useAuth();
  const { showToast } = useToast();
  const [alerts, setAlerts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  const canResolveAlerts = ALERT_RESOLVE_ROLES.includes(currentUser?.role);

  const canAcknowledgeAlert = (alert) => {
    if (["ADMIN", "LOGISTICS_PROVIDER", "DEALERSHIP"].includes(currentUser?.role)) {
      return true;
    }

    return (
      currentUser?.role === "MANUFACTURER" &&
      MANUFACTURER_ACKNOWLEDGE_TYPES.includes(alert.alertType)
    );
  };

  const loadOpenAlerts = useCallback(async () => {
    try {
      setIsLoading(true);
      setErrorMessage("");

      const data = await alertApi.getOpenAlerts();
      setAlerts(data);
    } catch (error) {
      const message =
        error?.response?.data?.message || "Failed to load alerts.";
      setErrorMessage(message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadOpenAlerts();
  }, [loadOpenAlerts]);

  const handleAcknowledge = async (alertId) => {
    try {
      setErrorMessage("");
      const updatedAlert = await alertApi.acknowledge(alertId);
      showToast({
        type: "success",
        title: "Alert acknowledged",
        message: `${updatedAlert.referenceLabel} is now marked as acknowledged.`,
      });
      window.dispatchEvent(new Event(alertCountRefreshEventName));
      await loadOpenAlerts();
    } catch (error) {
      const message =
        error?.response?.data?.message || "Failed to acknowledge alert.";
      setErrorMessage(message);
      showToast({
        type: "error",
        title: "Alert action failed",
        message,
      });
    }
  };

  const handleResolve = async (alertId) => {
    try {
      setErrorMessage("");
      const updatedAlert = await alertApi.resolve(alertId);
      showToast({
        type: "success",
        title: "Alert resolved",
        message: `${updatedAlert.referenceLabel} is no longer open.`,
      });
      window.dispatchEvent(new Event(alertCountRefreshEventName));
      await loadOpenAlerts();
    } catch (error) {
      const message =
        error?.response?.data?.message || "Failed to resolve alert.";
      setErrorMessage(message);
      showToast({
        type: "error",
        title: "Alert action failed",
        message,
      });
    }
  };

  if (isLoading && alerts.length === 0) {
    return <p>Loading alerts...</p>;
  }

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>Open Alerts</h2>
          <p>Review active shipment, order, and stock alerts.</p>
        </div>

        <button type="button" disabled={isLoading} onClick={loadOpenAlerts}>
          {isLoading ? "Refreshing..." : "Refresh"}
        </button>
      </div>

      {errorMessage && <p className="error-message">{errorMessage}</p>}

      <div className="panel">
        {alerts.length === 0 ? (
          <p>No open alerts.</p>
        ) : (
          alerts.map((alert) => (
            <div
              key={alert.id}
              className={`alert-item ${alert.alertType?.toLowerCase()}`}
            >
              <div>
                <strong>{alert.title}</strong>
                <p>{alert.message}</p>
                <div className="alert-meta">
                  {alert.referenceType === "SHIPMENT" ? (
                    <span>
                      Shipment:{" "}
                      <Link to={`/shipments/${alert.referenceId}`}>
                        {alert.referenceLabel}
                      </Link>
                    </span>
                  ) : (
                    <span>
                      Stock rule:{" "}
                      <Link to={`/stock-monitoring?rule=${alert.referenceId}`}>
                        {alert.referenceLabel}
                      </Link>
                    </span>
                  )}
                  <span>Type: {formatEnumLabel(alert.alertType)}</span>
                  <span>Created: {formatDateTime(alert.createdAt)}</span>
                </div>
              </div>

              <div className="alert-actions">
                <span className={`severity ${alert.severity?.toLowerCase()}`}>
                  {formatEnumLabel(alert.severity)}
                </span>

                {canAcknowledgeAlert(alert) && (
                  <button
                    type="button"
                    disabled={isLoading}
                    onClick={() => handleAcknowledge(alert.id)}
                  >
                    Acknowledge
                  </button>
                )}

                {canResolveAlerts && (
                  <button
                    type="button"
                    className="secondary-button"
                    disabled={isLoading}
                    onClick={() => handleResolve(alert.id)}
                  >
                    Resolve
                  </button>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default AlertsPage;
