export function canUserConfirmShipmentReceipt(shipment, user) {
  return Boolean(
    shipment &&
      user?.role === "DEALERSHIP" &&
      user.partyProfileKey &&
      user.partyProfileKey === shipment.dealershipKey &&
      !shipment.receiptConfirmedAt &&
      ["ARRIVED", "DELIVERED"].includes(shipment.status),
  );
}
