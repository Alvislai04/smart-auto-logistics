const STATUS_ACTIONS = [
  {
    label: "Mark Ready",
    status: "READY_FOR_DISPATCH",
    allowedFrom: ["CREATED"],
  },
  {
    label: "Mark Picked Up",
    status: "PICKED_UP",
    allowedFrom: ["READY_FOR_DISPATCH"],
  },
  {
    label: "Mark In Transit",
    status: "IN_TRANSIT",
    allowedFrom: ["PICKED_UP", "DELAYED"],
  },
  {
    label: "Mark Arrived",
    status: "ARRIVED",
    allowedFrom: ["IN_TRANSIT", "DELAYED"],
  },
  {
    label: "Cancel",
    status: "CANCELLED",
    allowedFrom: [
      "CREATED",
      "READY_FOR_DISPATCH",
      "PICKED_UP",
      "IN_TRANSIT",
      "DELAYED",
      "ARRIVED",
    ],
    danger: true,
  },
];

function ShipmentStatusActions({ currentStatus, isUpdating, onUpdateStatus }) {
  const availableActions = STATUS_ACTIONS.filter((action) =>
    action.allowedFrom.includes(currentStatus),
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
