import { useEffect, useState } from "react";
import { alertApi } from "../api/alertApi";

function AlertsPage() {
  const [alerts, setAlerts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    loadOpenAlerts();
  }, []);

  const loadOpenAlerts = async () => {
    try {
      setIsLoading(true);
      setErrorMessage("");

      const data = await alertApi.getOpenAlerts();
      setAlerts(data);
    } catch (error) {
      setErrorMessage("Failed to load alerts.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleAcknowledge = async (alertId) => {
    await alertApi.acknowledge(alertId);
    await loadOpenAlerts();
  };

  const handleResolve = async (alertId) => {
    await alertApi.resolve(alertId);
    await loadOpenAlerts();
  };

  if (isLoading) {
    return <p>Loading alerts...</p>;
  }

  if (errorMessage) {
    return <p className="error-message">{errorMessage}</p>;
  }

  return (
    <div className="page">
      <div className="page-header">
        <h2>Open Alerts</h2>
        <button onClick={loadOpenAlerts}>Refresh</button>
      </div>

      <div className="panel">
        {alerts.length === 0 ? (
          <p>No open alerts.</p>
        ) : (
          alerts.map((alert) => (
            <div key={alert.id} className="alert-item">
              <div>
                <strong>{alert.title}</strong>
                <p>{alert.message}</p>
                <small>
                  Shipment: {alert.shipmentCode} | Type: {alert.alertType}
                </small>
              </div>

              <div className="alert-actions">
                <span className={`severity ${alert.severity?.toLowerCase()}`}>
                  {alert.severity}
                </span>

                <button onClick={() => handleAcknowledge(alert.id)}>
                  Acknowledge
                </button>

                <button onClick={() => handleResolve(alert.id)}>Resolve</button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default AlertsPage;
