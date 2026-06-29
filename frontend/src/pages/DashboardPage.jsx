import { useEffect, useState } from "react";
import { dashboardApi } from "../api/dashboardApi";
import { alertApi } from "../api/alertApi";
import SummaryCard from "../components/dashboard/SummaryCard";
import OpenAlertsPanel from "../components/dashboard/OpenAlertsPanel";

function DashboardPage() {
  const [summary, setSummary] = useState(null);
  const [alerts, setAlerts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setIsLoading(true);
      setErrorMessage("");

      const [summaryData, openAlerts] = await Promise.all([
        dashboardApi.getSummary(),
        alertApi.getOpenAlerts(),
      ]);

      setSummary(summaryData);
      setAlerts(openAlerts);
    } catch (error) {
      setErrorMessage("Failed to load dashboard data.");
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return <p>Loading dashboard...</p>;
  }

  if (errorMessage) {
    return <p className="error-message">{errorMessage}</p>;
  }

  return (
    <div className="page">
      <div className="page-header">
        <h2>Dashboard</h2>
        <button onClick={loadDashboardData}>Refresh</button>
      </div>

      <div className="summary-grid">
        <SummaryCard
          title="Total Shipments"
          value={summary.totalShipments}
          description="All registered shipments"
        />

        <SummaryCard
          title="In Transit"
          value={summary.inTransitShipments}
          description="Currently moving vehicles"
        />

        <SummaryCard
          title="Delayed"
          value={summary.delayedShipments}
          description="Shipments requiring attention"
        />

        <SummaryCard
          title="Delivered"
          value={summary.deliveredShipments}
          description="Completed shipments"
        />

        <SummaryCard
          title="Open Alerts"
          value={summary.openAlerts}
          description="Unresolved logistics alerts"
        />
      </div>

      <OpenAlertsPanel alerts={alerts} />
    </div>
  );
}

export default DashboardPage;
