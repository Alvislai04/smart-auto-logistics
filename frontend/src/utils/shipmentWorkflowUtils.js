export function canUserConfirmShipmentReceipt(shipment, user) {
  const shipmentHasArrived =
    ["ARRIVED", "DELIVERED"].includes(shipment?.status) ||
    shipment?.currentStage === "ARRIVED_AT_DEALERSHIP" ||
    shipment?.currentStage === "DELIVERED";

  return Boolean(
    shipment &&
    user?.role === "DEALERSHIP" &&
    user.partyProfileKey &&
    user.partyProfileKey === shipment.dealershipKey &&
    !shipment.receiptConfirmedAt &&
    shipmentHasArrived,
  );
}
