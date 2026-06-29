import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { shipmentApi } from "../api/shipmentApi";
import { trackingApi } from "../api/trackingApi";
import { alertApi } from "../api/alertApi";
import ShipmentMap from "../components/map/ShipmentMap";
import ShipmentStatusActions from "../components/shipments/ShipmentStatusActions";
import { shipmentEventApi } from "../api/shipmentEventApi";
import { useAuth } from "../auth/AuthContext";

function ShipmentDetailsPage() {
  const { shipmentId } = useParams();
  const { currentUser } = useAuth();

  const [shipment, setShipment] = useState(null);
  const [latestTracking, setLatestTracking] = useState(null);
  const [trackingEvents, setTrackingEvents] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [shipmentEvents, setShipmentEvents] = useState([]);
  const [simulationInput, setSimulationInput] = useState({
    currentLatitude: 3.139,
    currentLongitude: 101.6869,
    speed: 55.0,
  });
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [isUpdatingStatus, setIsUpdatingStatus] = useState(false);

  useEffect(() => {
    loadShipmentDetails();
  }, [shipmentId]);

  const loadShipmentDetails = async () => {
    try {
      setIsLoading(true);
      setErrorMessage("");

      const shipmentData = await shipmentApi.getById(shipmentId);
      const trackingData = await trackingApi.getByShipmentId(shipmentId);
      const alertData = await alertApi.getByShipmentId(shipmentId);
      const eventData = await shipmentEventApi.getByShipmentId(shipmentId);

      setShipment(shipmentData);
      setTrackingEvents(trackingData);
      setAlerts(alertData);
      setShipmentEvents(eventData);

      try {
        const latestData = await trackingApi.getLatestByShipmentId(shipmentId);
        setLatestTracking(latestData);
      } catch {
        setLatestTracking(null);
      }
    } catch (error) {
      setErrorMessage("Failed to load shipment details.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleSimulationChange = (event) => {
    const { name, value } = event.target;

    setSimulationInput((previousValue) => ({
      ...previousValue,
      [name]: Number(value),
    }));
  };

  const handleSimulateNextPoint = async () => {
    try {
      await trackingApi.simulateNextPoint(shipmentId, simulationInput);
      await loadShipmentDetails();
    } catch (error) {
      setErrorMessage("Failed to simulate next tracking point.");
    }
  };

  const handleUpdateStatus = async (nextStatus) => {
    try {
      setIsUpdatingStatus(true);
      setErrorMessage("");

      await shipmentApi.updateStatus(shipmentId, nextStatus);
      await loadShipmentDetails();
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message || "Failed to update shipment status.",
      );
    } finally {
      setIsUpdatingStatus(false);
    }
  };

  if (isLoading) {
    return <p>Loading shipment details...</p>;
  }

  if (errorMessage) {
    return <p className="error-message">{errorMessage}</p>;
  }

  if (!shipment) {
    return <p>Shipment not found.</p>;
  }

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>{shipment.shipmentCode}</h2>
          <p>
            {shipment.vehicleModel} — {shipment.vehicleVin}
          </p>
        </div>

        <button onClick={loadShipmentDetails}>Refresh</button>
      </div>

      <div className="panel">
        <h3>Live Vehicle Location</h3>
        <ShipmentMap
          latestTracking={latestTracking}
          trackingEvents={trackingEvents}
          shipment={shipment}
        />
      </div>

      <div className="details-grid">
        <div className="panel">
          <h3>Shipment Information</h3>

          <p>
            <strong>Status:</strong>{" "}
            <span className={`status ${shipment.status?.toLowerCase()}`}>
              {shipment.status}
            </span>
          </p>

          <p>
            <strong>Origin:</strong> {shipment.origin}
          </p>

          <p>
            <strong>Destination:</strong> {shipment.destination}
          </p>

          <p>
            <strong>Dealership:</strong> {shipment.dealershipName}
          </p>

          <p>
            <strong>ETA:</strong> {shipment.estimatedArrival}
          </p>

          {["ADMIN", "LOGISTICS_PROVIDER"].includes(currentUser?.role) && (
            <div className="status-action-section">
              <h4>Shipment Workflow Actions</h4>

              <ShipmentStatusActions
                currentStatus={shipment.status}
                isUpdating={isUpdatingStatus}
                onUpdateStatus={handleUpdateStatus}
              />
            </div>
          )}
        </div>

        <div className="panel">
          <h3>Latest Tracking</h3>
          {latestTracking ? (
            <>
              <p>
                <strong>Latitude:</strong> {latestTracking.latitude}
              </p>
              <p>
                <strong>Longitude:</strong> {latestTracking.longitude}
              </p>
              <p>
                <strong>Speed:</strong> {latestTracking.speed} km/h
              </p>
              <p>
                <strong>Condition:</strong> {latestTracking.conditionStatus}
              </p>
              <p>
                <strong>Recorded At:</strong> {latestTracking.recordedAt}
              </p>
            </>
          ) : (
            <p>No tracking data yet.</p>
          )}
        </div>
      </div>

      {["ADMIN", "LOGISTICS_PROVIDER"].includes(currentUser?.role) && (
        <div className="panel">
          <h3>Simulate IoT Tracking Update</h3>

          <div className="form-grid">
            <label>
              Current Latitude
              <input
                type="number"
                step="0.0001"
                name="currentLatitude"
                value={simulationInput.currentLatitude}
                onChange={handleSimulationChange}
              />
            </label>

            <label>
              Current Longitude
              <input
                type="number"
                step="0.0001"
                name="currentLongitude"
                value={simulationInput.currentLongitude}
                onChange={handleSimulationChange}
              />
            </label>

            <label>
              Speed
              <input
                type="number"
                step="0.1"
                name="speed"
                value={simulationInput.speed}
                onChange={handleSimulationChange}
              />
            </label>
          </div>

          <button onClick={handleSimulateNextPoint}>Simulate Next Point</button>
        </div>
      )}

      <div className="panel">
        <h3>Alerts</h3>

        {alerts.length === 0 ? (
          <p>No alerts for this shipment.</p>
        ) : (
          alerts.map((alert) => (
            <div key={alert.id} className="alert-item">
              <div>
                <strong>{alert.title}</strong>
                <p>{alert.message}</p>
                <small>Status: {alert.status}</small>
              </div>

              <span className={`severity ${alert.severity?.toLowerCase()}`}>
                {alert.severity}
              </span>
            </div>
          ))
        )}
      </div>

      <div className="panel">
        <h3>Shipment Event Timeline</h3>

        {shipmentEvents.length === 0 ? (
          <p>No shipment events recorded yet.</p>
        ) : (
          <div className="event-timeline">
            {shipmentEvents.map((event) => (
              <div key={event.id} className="event-item">
                <div>
                  <strong>{event.eventType}</strong>
                  <p>{event.description}</p>

                  {event.previousStatus && event.newStatus && (
                    <small>
                      {event.previousStatus} → {event.newStatus}
                    </small>
                  )}
                </div>

                <span>{event.createdAt}</span>
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="panel">
        <h3>Tracking History</h3>

        <div className="table-card">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Latitude</th>
                <th>Longitude</th>
                <th>Speed</th>
                <th>Condition</th>
                <th>Recorded At</th>
              </tr>
            </thead>

            <tbody>
              {trackingEvents.map((event) => (
                <tr key={event.id}>
                  <td>{event.id}</td>
                  <td>{event.latitude}</td>
                  <td>{event.longitude}</td>
                  <td>{event.speed}</td>
                  <td>{event.conditionStatus}</td>
                  <td>{event.recordedAt}</td>
                </tr>
              ))}
            </tbody>
          </table>

          {trackingEvents.length === 0 && <p>No tracking history yet.</p>}
        </div>
      </div>
    </div>
  );
}

export default ShipmentDetailsPage;
