import {
  MapContainer,
  Marker,
  Polyline,
  Popup,
  TileLayer,
} from "react-leaflet";
import L from "leaflet";

const vehicleMarkerIcon = new L.Icon({
  iconUrl:
    "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png",
  iconRetinaUrl:
    "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png",
  shadowUrl:
    "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png",
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});

function ShipmentMap({ latestTracking, trackingEvents = [], shipment }) {
  if (!latestTracking) {
    return (
      <div className="map-placeholder">
        <p>No tracking location available yet.</p>
      </div>
    );
  }

  const latestPosition = [latestTracking.latitude, latestTracking.longitude];

  const routePositions = trackingEvents
    .filter((event) => event.latitude !== null && event.longitude !== null)
    .map((event) => [event.latitude, event.longitude]);

  return (
    <div className="map-wrapper">
      <MapContainer
        center={latestPosition}
        zoom={13}
        scrollWheelZoom={false}
        className="shipment-map"
      >
        <TileLayer
          attribution="&copy; OpenStreetMap contributors"
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {routePositions.length >= 2 && (
          <Polyline
            positions={routePositions}
            pathOptions={{
              weight: 4,
              opacity: 0.8,
            }}
          />
        )}

        <Marker position={latestPosition} icon={vehicleMarkerIcon}>
          <Popup>
            <strong>{shipment?.shipmentCode || "Shipment"}</strong>
            <br />
            Vehicle: {shipment?.vehicleModel || "-"}
            <br />
            Speed: {latestTracking.speed} km/h
            <br />
            Condition: {latestTracking.conditionStatus}
          </Popup>
        </Marker>
      </MapContainer>
    </div>
  );
}

export default ShipmentMap;
