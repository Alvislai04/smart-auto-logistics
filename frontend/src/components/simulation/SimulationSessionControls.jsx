import { formatCoordinate, formatDateTime, formatEnumLabel } from "../../utils/formatters";

const activeSessionStatuses = ["ACTIVE", "PAUSED"];

function SimulationSessionControls({
  session,
  options,
  formValues,
  onFormChange,
  onStart,
  onPause,
  onResume,
  onStop,
  isBusy,
}) {
  const hasOpenSession = activeSessionStatuses.includes(session?.status);

  return (
    <div className="simulation-control-grid">
      <div className="simulation-session-summary">
        <div className="simulation-status-row">
          <span className={`status-badge ${session?.status?.toLowerCase() ?? "idle"}`}>
            {session ? formatEnumLabel(session.status) : "Not Started"}
          </span>
          {session?.shipmentCode && <strong>{session.shipmentCode}</strong>}
        </div>

        <div className="detail-list compact">
          <div className="detail-item">
            <span>Generated Events</span>
            <strong>
              {session?.generatedEvents ?? 0}
              {session?.maxEvents ? ` / ${session.maxEvents}` : ""}
            </strong>
          </div>
          <div className="detail-item">
            <span>Current Position</span>
            <strong>
              {formatCoordinate(session?.currentLatitude)},{" "}
              {formatCoordinate(session?.currentLongitude)}
            </strong>
          </div>
          <div className="detail-item">
            <span>Next Run</span>
            <strong>{formatDateTime(session?.nextRunAt)}</strong>
          </div>
          <div className="detail-item">
            <span>Last Run</span>
            <strong>{formatDateTime(session?.lastRunAt)}</strong>
          </div>
        </div>
      </div>

      <form className="simulation-form" onSubmit={onStart}>
        <div className="form-grid simulation-form-grid">
          <label>
            Interval Seconds
            <input
              type="number"
              name="intervalSeconds"
              min={options?.minIntervalSeconds ?? 5}
              max={options?.maxIntervalSeconds ?? 3600}
              value={formValues.intervalSeconds}
              onChange={onFormChange}
              disabled={hasOpenSession || isBusy}
              required
            />
          </label>

          <label>
            Speed km/h
            <input
              type="number"
              name="speed"
              min={options?.minSpeed ?? 0}
              max={options?.maxSpeed ?? 160}
              step="0.1"
              value={formValues.speed}
              onChange={onFormChange}
              disabled={hasOpenSession || isBusy}
              required
            />
          </label>

          <label>
            Condition
            <select
              name="conditionStatus"
              value={formValues.conditionStatus}
              onChange={onFormChange}
              disabled={hasOpenSession || isBusy}
            >
              <option value="">Automatic from speed</option>
              {options?.conditionOptions?.map((option) => (
                <option key={option.conditionKey} value={option.conditionKey}>
                  {option.displayName}
                </option>
              ))}
            </select>
          </label>

          <label>
            Max Events
            <input
              type="number"
              name="maxEvents"
              min="1"
              max={options?.maxGeneratedEvents ?? 500}
              value={formValues.maxEvents}
              onChange={onFormChange}
              disabled={hasOpenSession || isBusy}
              placeholder="No limit"
            />
          </label>
        </div>

        <div className="simulation-actions">
          {!hasOpenSession && (
            <button type="submit" disabled={isBusy}>
              Start
            </button>
          )}

          {session?.status === "ACTIVE" && (
            <button type="button" onClick={onPause} disabled={isBusy}>
              Pause
            </button>
          )}

          {session?.status === "PAUSED" && (
            <button type="button" onClick={onResume} disabled={isBusy}>
              Resume
            </button>
          )}

          {hasOpenSession && (
            <button
              type="button"
              className="danger-button"
              onClick={onStop}
              disabled={isBusy}
            >
              Stop
            </button>
          )}
        </div>
      </form>
    </div>
  );
}

export default SimulationSessionControls;
