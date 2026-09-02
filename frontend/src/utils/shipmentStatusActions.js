const SHIPMENT_STATUS_ACTIONS = [
  {
    label: "Start Transit",
    status: "IN_TRANSIT",
    allowedFrom: ["CREATED", "READY_FOR_DISPATCH", "PICKED_UP"],
    allowedRoles: ["LOGISTICS_PROVIDER"],
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
    ],
    allowedRoles: ["LOGISTICS_PROVIDER"],
    danger: true,
  },
];

export function getAvailableShipmentStatusActions(
  currentStatus,
  currentUserRole,
) {
  return SHIPMENT_STATUS_ACTIONS.filter(
    (action) =>
      action.allowedFrom.includes(currentStatus) &&
      action.allowedRoles.includes(currentUserRole),
  );
}

export function hasAvailableShipmentStatusActions(
  currentStatus,
  currentUserRole,
) {
  return (
    getAvailableShipmentStatusActions(currentStatus, currentUserRole).length > 0
  );
}
