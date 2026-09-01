import { describe, expect, it } from "vitest";
import { canUserConfirmShipmentReceipt } from "./shipmentWorkflowUtils";

const arrivedShipment = {
  status: "ARRIVED",
  dealershipKey: "DLR-KL-001",
  receiptConfirmedAt: null,
};

describe("canUserConfirmShipmentReceipt", () => {
  it("allows the assigned dealership to confirm an arrived shipment", () => {
    expect(
      canUserConfirmShipmentReceipt(arrivedShipment, {
        role: "DEALERSHIP",
        partyProfileKey: "DLR-KL-001",
      }),
    ).toBe(true);
  });

  it("rejects logistics providers and unassigned dealerships", () => {
    expect(
      canUserConfirmShipmentReceipt(arrivedShipment, {
        role: "LOGISTICS_PROVIDER",
        partyProfileKey: "LOGISTICS-001",
      }),
    ).toBe(false);
    expect(
      canUserConfirmShipmentReceipt(arrivedShipment, {
        role: "DEALERSHIP",
        partyProfileKey: "DLR-PENANG-002",
      }),
    ).toBe(false);
  });

  it("rejects confirmation before arrival or after receipt is recorded", () => {
    expect(
      canUserConfirmShipmentReceipt(
        { ...arrivedShipment, status: "IN_TRANSIT" },
        { role: "DEALERSHIP", partyProfileKey: "DLR-KL-001" },
      ),
    ).toBe(false);
    expect(
      canUserConfirmShipmentReceipt(
        { ...arrivedShipment, receiptConfirmedAt: "2026-09-01T10:00:00" },
        { role: "DEALERSHIP", partyProfileKey: "DLR-KL-001" },
      ),
    ).toBe(false);
  });
});
