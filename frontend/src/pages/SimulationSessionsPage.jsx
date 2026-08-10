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
  const [isActionRunning, setIsActionRunning] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

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
        description: "All recorded simulation sessions",
      },
      {
        title: "Active",
        value: countByStatus.ACTIVE ?? 0,
        description: "Generating tracking events",
      },
      {
        title: "Paused",
        value: countByStatus.PAUSED ?? 0,
        description: "Waiting for resume",
      },
      {
        title: "Completed",
        value: countByStatus.COMPLETED ?? 0,
        description: "Reached destination or max events",
      },
    ];
  }, [sessions]);

  const runSessionAction = async (action, successText, failureText) => {
    try {
      setIsActionRunning(true);
      setErrorMessage("");
      setSuccessMessage("");

      await action();
      setSuccessMessage(successText);
      await loadSessions(false);
    } catch (error) {
      setErrorMessage(error.response?.data?.message || failureText);
    } finally {
      setIsActionRunning(false);
    }
  };

  const handlePause = (sessionId) =>
    runSessionAction(
      () => simulationApi.pauseSession(sessionId),
      "Simulation session paused.",
      "Failed to pause simulation session.",
    );

  const handleResume = (sessionId) =>
    runSessionAction(
      () => simulationApi.resumeSession(sessionId),
      "Simulation session resumed.",
      "Failed to resume simulation session.",
    );

  const handleStop = (sessionId) =>
    runSessionAction(
      () => simulationApi.stopSession(sessionId),
      "Simulation session stopped.",
      "Failed to stop simulation session.",
    );

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
      {successMessage && <p className="success-message">{successMessage}</p>}

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
                <th>Speed</th>
                <th>Position</th>
                <th>Generated</th>
                <th>Last Run</th>
                <th>Next Run</th>
                <th>Actions</th>
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
                  <td>
                    <div className="simulation-table-actions">
                      {session.status === "ACTIVE" && (
                        <button
                          type="button"
                          disabled={isActionRunning}
                          onClick={() => handlePause(session.id)}
                        >
                          Pause
                        </button>
                      )}

                      {session.status === "PAUSED" && (
                        <button
                          type="button"
                          disabled={isActionRunning}
                          onClick={() => handleResume(session.id)}
                        >
                          Resume
                        </button>
                      )}

                      {["ACTIVE", "PAUSED"].includes(session.status) && (
                        <button
                          type="button"
                          className="danger-button"
                          disabled={isActionRunning}
                          onClick={() => handleStop(session.id)}
                        >
                          Stop
                        </button>
                      )}
                    </div>
                  </td>
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
