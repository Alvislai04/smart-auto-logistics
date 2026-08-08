import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { alertApi } from "../api/alertApi";
import { blockchainApi } from "../api/blockchainApi";
import { shipmentApi } from "../api/shipmentApi";
import { shipmentEventApi } from "../api/shipmentEventApi";
import { trackingApi } from "../api/trackingApi";
import { useAuth } from "../auth/AuthContext";
import BlockchainRecordPanel from "../components/blockchain/BlockchainRecordPanel";
import ShipmentMap from "../components/map/ShipmentMap";
import ShipmentStatusActions from "../components/shipments/ShipmentStatusActions";
import {
  formatCoordinate,
  formatDateTime,
  formatEnumLabel,
} from "../utils/formatters";

const receiptConfirmationRoles = ["ADMIN", "LOGISTICS_PROVIDER", "DEALERSHIP"];

function DetailItem({ label, children }) {
  return (
    <p className="detail-item">
      <span>{label}</span>
      <strong>{children || "-"}</strong>
    </p>
  );
}

function ShipmentDetailsPage() {
  const { shipmentId } = useParams();
  const { currentUser } = useAuth();

  const [shipment, setShipment] = useState(null);
  const [latestTracking, setLatestTracking] = useState(null);
  const [trackingEvents, setTrackingEvents] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [shipmentEvents, setShipmentEvents] = useState([]);
  const [blockchainRecords, setBlockchainRecords] = useState([]);
  const [simulationInput, setSimulationInput] = useState({
    currentLatitude: 3.139,
    currentLongitude: 101.6869,
    speed: 55.0,
  });
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [isUpdatingStatus, setIsUpdatingStatus] = useState(false);
  const [isConfirmingReceipt, setIsConfirmingReceipt] = useState(false);

  const loadShipmentDetails = useCallback(async () => {
    try {
      setIsLoading(true);
      setErrorMessage("");

      const [shipmentData, trackingData, alertData, eventData] =
        await Promise.all([
          shipmentApi.getById(shipmentId),
          trackingApi.getByShipmentId(shipmentId),
          alertApi.getByShipmentId(shipmentId),
          shipmentEventApi.getByShipmentId(shipmentId),
        ]);

      setShipment(shipmentData);
      setTrackingEvents(trackingData);
      setAlerts(alertData);
      setShipmentEvents(eventData);
      setLatestTracking(trackingData.at(-1) ?? null);

      try {
        const blockchainData = await blockchainApi.getByShipmentId(shipmentId);
        setBlockchainRecords(blockchainData);
      } catch {
        setBlockchainRecords([]);
      }
    } catch {
      setErrorMessage("Failed to load shipment details.");
    } finally {
      setIsLoading(false);
    }
  }, [shipmentId]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadShipmentDetails();
  }, [loadShipmentDetails]);

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
    } catch {
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

  const handleConfirmReceipt = async () => {
    try {
      setIsConfirmingReceipt(true);
      setErrorMessage("");

      await shipmentApi.confirmReceipt(shipmentId, {
        confirmedBy: currentUser?.name || currentUser?.email || "System User",
      });
      await loadShipmentDetails();
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message || "Failed to confirm delivery receipt.",
      );
    } finally {
      setIsConfirmingReceipt(false);
    }
  };

  if (isLoading) {
    return <p>Loading shipment details...</p>;
  }

  if (errorMessage && !shipment) {
    return <p className="error-message">{errorMessage}</p>;
  }

  if (!shipment) {
    return <p>Shipment not found.</p>;
  }

  const canConfirmReceipt =
    receiptConfirmationRoles.includes(currentUser?.role) &&
    !shipment.receiptConfirmedAt &&
    ["ARRIVED", "DELIVERED"].includes(shipment.status);

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>{shipment.shipmentCode}</h2>
          <p>
            {shipment.vehicleModel} - {shipment.vehicleVin}
          </p>
        </div>

        <button type="button" onClick={loadShipmentDetails}>
          Refresh
        </button>
      </div>

      {errorMessage && <p className="error-message">{errorMessage}</p>}

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

          <div className="detail-list">
            <DetailItem label="Status">
              <span className={`status ${shipment.status?.toLowerCase()}`}>
                {formatEnumLabel(shipment.status)}
              </span>
            </DetailItem>
            <DetailItem label="Origin">{shipment.origin}</DetailItem>
            <DetailItem label="Destination">{shipment.destination}</DetailItem>
            <DetailItem label="Dealership">
              {shipment.dealershipName}
            </DetailItem>
            <DetailItem label="Document Ref.">
              {shipment.shipmentDocumentReference}
            </DetailItem>
          </div>

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
          <h3>Schedule and Operations</h3>

          <div className="detail-list">
            <DetailItem label="ETD">
              {formatDateTime(shipment.estimatedDeparture)}
            </DetailItem>
            <DetailItem label="ETA">
              {formatDateTime(shipment.estimatedArrival)}
            </DetailItem>
            <DetailItem label="Current Stage">
              {formatEnumLabel(shipment.currentStage)}
            </DetailItem>
            <DetailItem label="Stock Location">
              {shipment.stockLocation}
            </DetailItem>
            <DetailItem label="WIP Status">
              {formatEnumLabel(shipment.workInProgressStatus)}
            </DetailItem>
            <DetailItem label="Responsible Person">
              {shipment.responsiblePerson}
            </DetailItem>
            <DetailItem label="Responsible Contact">
              {shipment.responsibleContact}
            </DetailItem>
          </div>
        </div>

        <div className="panel">
          <h3>Route Monitoring</h3>

          <div className="detail-list">
            <DetailItem label="Origin Coordinates">
              {formatCoordinate(shipment.originLatitude)},{" "}
              {formatCoordinate(shipment.originLongitude)}
            </DetailItem>
            <DetailItem label="Destination Coordinates">
              {formatCoordinate(shipment.destinationLatitude)},{" "}
              {formatCoordinate(shipment.destinationLongitude)}
            </DetailItem>
            <DetailItem label="Monitoring Rules">
              ETA overdue, missing update, route deviation, progress shortfall
            </DetailItem>
          </div>
        </div>

        <div className="panel">
          <h3>Receiver and Receipt</h3>

          <div className="detail-list">
            <DetailItem label="Customer">{shipment.customerName}</DetailItem>
            <DetailItem label="Contact">{shipment.customerContact}</DetailItem>
            <DetailItem label="Address">{shipment.customerAddress}</DetailItem>
          </div>

          <div
            className={`receipt-card ${
              shipment.receiptConfirmedAt ? "confirmed" : "pending"
            }`}
          >
            {shipment.receiptConfirmedAt ? (
              <>
                <strong>Receipt Confirmed</strong>
                <span>
                  {formatDateTime(shipment.receiptConfirmedAt)} by{" "}
                  {shipment.receiptConfirmedBy}
                </span>
              </>
            ) : (
              <>
                <strong>Receipt Pending</strong>
                <span>Awaiting receiver confirmation.</span>

                {canConfirmReceipt && (
                  <button
                    type="button"
                    disabled={isConfirmingReceipt}
                    onClick={handleConfirmReceipt}
                  >
                    {isConfirmingReceipt ? "Confirming..." : "Confirm Receipt"}
                  </button>
                )}
              </>
            )}
          </div>
        </div>

        <div className="panel">
          <h3>Latest Tracking</h3>
          {latestTracking ? (
            <div className="detail-list">
              <DetailItem label="Latitude">{latestTracking.latitude}</DetailItem>
              <DetailItem label="Longitude">
                {latestTracking.longitude}
              </DetailItem>
              <DetailItem label="Speed">{latestTracking.speed} km/h</DetailItem>
              <DetailItem label="Condition">
                {formatEnumLabel(latestTracking.conditionStatus)}
              </DetailItem>
              <DetailItem label="Recorded At">
                {formatDateTime(latestTracking.recordedAt)}
              </DetailItem>
            </div>
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

          <button type="button" onClick={handleSimulateNextPoint}>
            Simulate Next Point
          </button>
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
                <small>Status: {formatEnumLabel(alert.status)}</small>
              </div>

              <span className={`severity ${alert.severity?.toLowerCase()}`}>
                {formatEnumLabel(alert.severity)}
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
                  <strong>{formatEnumLabel(event.eventType)}</strong>
                  <p>{event.description}</p>

                  {event.previousStatus && event.newStatus && (
                    <small>
                      {formatEnumLabel(event.previousStatus)} -&gt;{" "}
                      {formatEnumLabel(event.newStatus)}
                    </small>
                  )}
                </div>

                <span>{formatDateTime(event.createdAt)}</span>
              </div>
            ))}
          </div>
        )}
      </div>

      <BlockchainRecordPanel blockchainRecords={blockchainRecords} />

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
                  <td>{formatEnumLabel(event.conditionStatus)}</td>
                  <td>{formatDateTime(event.recordedAt)}</td>
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
