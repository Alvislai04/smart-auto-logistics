import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { simulationApi } from "../api/simulationApi";
import SummaryCard from "../components/dashboard/SummaryCard";
import {
  formatCoordinate,
  formatDateTime,
  formatEnumLabel,
} from "../utils/formatters";

function SimulationSessionsPage() {
  const [sessions, setSessions] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  const loadSessions = useCallback(async (showLoading = true) => {
    try {
      if (showLoading) {
        setIsLoading(true);
      }

      setErrorMessage("");
      const data = await simulationApi.getSessions();
      setSessions(data);
    } catch {
      setErrorMessage("Failed to load simulation sessions.");
    } finally {
      if (showLoading) {
        setIsLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadSessions();
  }, [loadSessions]);

  useEffect(() => {
    const refreshIntervalId = window.setInterval(() => {
      loadSessions(false);
    }, 6000);

    return () => window.clearInterval(refreshIntervalId);
  }, [loadSessions]);

  const summaryCards = useMemo(() => {
    const countByStatus = sessions.reduce((counts, session) => {
      counts[session.status] = (counts[session.status] ?? 0) + 1;
      return counts;
    }, {});

    return [
      {
        title: "Total Sessions",
        value: sessions.length,
        description: "System telemetry sessions",
      },
      {
        title: "Active",
        value: countByStatus.ACTIVE ?? 0,
        description: "Generating tracking events",
      },
      {
        title: "Delayed",
        value: sessions.filter(
          (session) => session.conditionStatus === "DELAYED",
        ).length,
        description: "Latest condition delayed",
      },
      {
        title: "Completed",
        value: countByStatus.COMPLETED ?? 0,
        description: "Reached destination or max events",
      },
    ];
  }, [sessions]);

  if (isLoading) {
    return <p>Loading simulation sessions...</p>;
  }

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>IoT Simulation</h2>
          <p>Active vehicle telemetry sessions</p>
        </div>

        <button type="button" onClick={() => loadSessions()}>
          Refresh
        </button>
      </div>

      {errorMessage && <p className="error-message">{errorMessage}</p>}

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

      <div className="panel">
        <h3>Simulation Sessions</h3>

        <div className="table-card">
          <table>
            <thead>
              <tr>
                <th>Shipment</th>
                <th>Status</th>
                <th>Interval</th>
                <th>Scenario</th>
                <th>Condition</th>
                <th>Speed</th>
                <th>Position</th>
                <th>Generated</th>
                <th>Last Run</th>
                <th>Next Run</th>
              </tr>
            </thead>

            <tbody>
              {sessions.map((session) => (
                <tr key={session.id}>
                  <td>
                    <Link to={`/shipments/${session.shipmentId}`}>
                      {session.shipmentCode}
                    </Link>
                    <small>{session.dealershipName}</small>
                  </td>
                  <td>
                    <span className={`status-badge ${session.status?.toLowerCase()}`}>
                      {formatEnumLabel(session.status)}
                    </span>
                    <small>{formatEnumLabel(session.shipmentStatus)}</small>
                  </td>
                  <td>{session.intervalSeconds}s</td>
                  <td>{session.lastProfileName || "-"}</td>
                  <td>{formatEnumLabel(session.conditionStatus)}</td>
                  <td>{session.speed} km/h</td>
                  <td>
                    {formatCoordinate(session.currentLatitude)},{" "}
                    {formatCoordinate(session.currentLongitude)}
                  </td>
                  <td>
                    {session.generatedEvents}
                    {session.maxEvents ? ` / ${session.maxEvents}` : ""}
                  </td>
                  <td>{formatDateTime(session.lastRunAt)}</td>
                  <td>{formatDateTime(session.nextRunAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>

          {sessions.length === 0 && <p>No simulation sessions yet.</p>}
        </div>
      </div>
    </div>
  );
}

export default SimulationSessionsPage;
