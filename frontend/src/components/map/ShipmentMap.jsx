import {
  CircleMarker,
  MapContainer,
  Marker,
  Polyline,
  Popup,
  TileLayer,
} from "react-leaflet";
import L from "leaflet";
import { formatEnumLabel } from "../../utils/formatters";

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

function hasCoordinate(value) {
  return value !== null && value !== undefined && Number.isFinite(Number(value));
}

function ShipmentMap({ latestTracking, trackingEvents = [], shipment }) {
  const hasLatestPosition =
    latestTracking?.latitude !== null &&
    latestTracking?.longitude !== null &&
    latestTracking?.latitude !== undefined &&
    latestTracking?.longitude !== undefined;
  const hasPlannedRoute =
    hasCoordinate(shipment?.originLatitude) &&
    hasCoordinate(shipment?.originLongitude) &&
    hasCoordinate(shipment?.destinationLatitude) &&
    hasCoordinate(shipment?.destinationLongitude);

  if (!hasLatestPosition && !hasPlannedRoute) {
    return (
      <div className="map-placeholder">
        <p>No tracking location available yet.</p>
      </div>
    );
  }

  const plannedOrigin = hasPlannedRoute
    ? [Number(shipment.originLatitude), Number(shipment.originLongitude)]
    : null;
  const plannedDestination = hasPlannedRoute
    ? [
        Number(shipment.destinationLatitude),
        Number(shipment.destinationLongitude),
      ]
    : null;
  const latestPosition = hasLatestPosition
    ? [latestTracking.latitude, latestTracking.longitude]
    : null;
  const mapCenter =
    latestPosition ??
    [
      (plannedOrigin[0] + plannedDestination[0]) / 2,
      (plannedOrigin[1] + plannedDestination[1]) / 2,
    ];

  const routePositions = trackingEvents
    .filter((event) => event.latitude !== null && event.longitude !== null)
    .map((event) => [event.latitude, event.longitude]);

  return (
    <div className="map-wrapper">
      <MapContainer
        center={mapCenter}
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

        {hasPlannedRoute && (
          <>
            <Polyline
              positions={[plannedOrigin, plannedDestination]}
              pathOptions={{
                color: "#0f766e",
                dashArray: "8 8",
                weight: 3,
                opacity: 0.85,
              }}
            />

            <CircleMarker
              center={plannedOrigin}
              radius={8}
              pathOptions={{ color: "#15803d", fillOpacity: 0.85 }}
            >
              <Popup>
                <strong>Planned Origin</strong>
                <br />
                {shipment.origin}
              </Popup>
            </CircleMarker>

            <CircleMarker
              center={plannedDestination}
              radius={8}
              pathOptions={{ color: "#dc2626", fillOpacity: 0.85 }}
            >
              <Popup>
                <strong>Planned Destination</strong>
                <br />
                {shipment.destination}
              </Popup>
            </CircleMarker>
          </>
        )}

        {latestPosition && (
          <Marker position={latestPosition} icon={vehicleMarkerIcon}>
            <Popup>
              <strong>{shipment?.shipmentCode || "Shipment"}</strong>
              <br />
              Vehicle: {shipment?.vehicleModel || "-"}
              <br />
              Speed: {latestTracking.speed} km/h
              <br />
              Condition: {formatEnumLabel(latestTracking.conditionStatus)}
            </Popup>
          </Marker>
        )}
      </MapContainer>
    </div>
  );
}

export default ShipmentMap;
