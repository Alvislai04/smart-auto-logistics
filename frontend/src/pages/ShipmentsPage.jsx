import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { shipmentApi } from "../api/shipmentApi";
import { useAuth } from "../auth/AuthContext";

function ShipmentsPage() {
  const { currentUser } = useAuth();
  const [shipments, setShipments] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    loadShipments();
  }, []);

  const loadShipments = async () => {
    try {
      setIsLoading(true);
      setErrorMessage("");

      const data = await shipmentApi.getAll();
      setShipments(data);
    } catch (error) {
      setErrorMessage("Failed to load shipments.");
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return <p>Loading shipments...</p>;
  }

  if (errorMessage) {
    return <p className="error-message">{errorMessage}</p>;
  }

  return (
    <div className="page">
      <div className="page-header">
        <h2>Shipments</h2>

        <div className="header-actions">
          {["ADMIN", "MANUFACTURER"].includes(currentUser?.role) && (
            <Link className="button-link" to="/shipments/create">
              Create Shipment
            </Link>
          )}

          <button onClick={loadShipments}>Refresh</button>
        </div>
      </div>

      <div className="table-card">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Shipment Code</th>
              <th>Vehicle</th>
              <th>Destination</th>
              <th>Status</th>
              <th>ETA</th>
              <th>Action</th>
            </tr>
          </thead>

          <tbody>
            {shipments.map((shipment) => (
              <tr key={shipment.id}>
                <td>{shipment.id}</td>
                <td>{shipment.shipmentCode}</td>
                <td>
                  {shipment.vehicleModel}
                  <br />
                  <small>{shipment.vehicleVin}</small>
                </td>
                <td>{shipment.destination}</td>
                <td>
                  <span className={`status ${shipment.status?.toLowerCase()}`}>
                    {shipment.status}
                  </span>
                </td>
                <td>{shipment.estimatedArrival}</td>
                <td>
                  <Link to={`/shipments/${shipment.id}`}>View</Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {shipments.length === 0 && <p>No shipments found.</p>}
      </div>
    </div>
  );
}

export default ShipmentsPage;
