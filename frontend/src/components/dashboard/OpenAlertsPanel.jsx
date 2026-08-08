import { formatEnumLabel } from "../../utils/formatters";

function OpenAlertsPanel({ alerts }) {
  if (!alerts || alerts.length === 0) {
    return (
      <div className="panel">
        <h3>Open Alerts</h3>
        <p>No open alerts at the moment.</p>
      </div>
    );
  }

  return (
    <div className="panel">
      <h3>Open Alerts</h3>

      <div className="alert-list">
        {alerts.map((alert) => (
          <div key={alert.id} className="alert-item">
            <div>
              <strong>{alert.title}</strong>
              <p>{alert.message}</p>
            </div>

            <span className={`severity ${alert.severity?.toLowerCase()}`}>
              {formatEnumLabel(alert.severity)}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}

export default OpenAlertsPanel;
