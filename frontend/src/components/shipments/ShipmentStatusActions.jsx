import { getAvailableShipmentStatusActions } from "../../utils/shipmentStatusActions";

function ShipmentStatusActions({
  currentStatus,
  currentUserRole,
  isUpdating,
  onUpdateStatus,
}) {
  const availableActions = getAvailableShipmentStatusActions(
    currentStatus,
    currentUserRole,
  );

  if (availableActions.length === 0) {
    return (
      <div className="status-actions-empty">
        <p>No status actions available for this shipment.</p>
      </div>
    );
  }

  return (
    <div className="status-actions">
      {availableActions.map((action) => (
        <button
          key={action.status}
          type="button"
          className={action.danger ? "danger-button" : ""}
          disabled={isUpdating}
          onClick={() => onUpdateStatus(action.status)}
        >
          {isUpdating ? "Updating..." : action.label}
        </button>
      ))}
    </div>
  );
}

export default ShipmentStatusActions;
