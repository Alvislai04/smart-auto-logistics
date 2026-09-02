import { afterEach, describe, expect, it, vi } from "vitest";

import {
  buildCreateShipmentPayload,
  buildShipmentPayload,
  emptyShipmentFormOptions,
  minimumFutureDateTimeLocalValue,
  scopeManufacturerOrigins,
  validateShipmentForm,
} from "./shipmentFormUtils";

const validFormData = {
  manufacturerKey: "MFG-001",
  dealershipKey: "DLR-001",
  vehicleVin: "WDD-TEST-0001",
  vehicleModelKey: "MODEL-001",
  origin: "Pekan, Pahang",
  destination: "Kuala Lumpur",
  dealershipName: "Test Dealership",
  estimatedArrival: "2026-08-27T12:00",
  estimatedDeparture: "2026-08-26T12:00",
  currentStage: "MANUFACTURING",
  stockLocation: "Pekan Yard",
  workInProgressStatus: "NOT_STARTED",
  responsiblePerson: "Test Operator",
  responsibleContact: "+60000000000",
  customerName: "Test Customer",
  customerContact: "+60000000001",
  customerAddress: "Test Address",
  shipmentDocumentReference: "DOC-001",
};

afterEach(() => {
  vi.useRealTimers();
});

describe("shipment form scheduling", () => {
  it("limits manufacturer origins to the authenticated company profile", () => {
    const options = {
      ...emptyShipmentFormOptions,
      manufacturerOrigins: [
        { key: "MFG-001", name: "Own Manufacturer" },
        { key: "MFG-002", name: "Other Manufacturer" },
      ],
    };

    expect(scopeManufacturerOrigins(options, "MFG-001").manufacturerOrigins)
      .toEqual([{ key: "MFG-001", name: "Own Manufacturer" }]);
    expect(scopeManufacturerOrigins(options, null).manufacturerOrigins).toEqual([]);
  });

  it("omits the server-managed shipment start from API payloads", () => {
    expect(buildShipmentPayload(validFormData)).not.toHaveProperty(
      "estimatedDeparture",
    );
  });

  it("omits system-managed stage and WIP state from creation payloads", () => {
    const payload = buildCreateShipmentPayload(validFormData);

    expect(payload).not.toHaveProperty("currentStage");
    expect(payload).not.toHaveProperty("workInProgressStatus");
  });

  it("uses the next local minute as the earliest selectable arrival", () => {
    expect(
      minimumFutureDateTimeLocalValue(new Date(2026, 7, 26, 12, 0, 30)),
    ).toBe("2026-08-26T12:01");
  });

  it("validates arrival against the current time during creation", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-08-26T04:00:00Z"));

    expect(
      validateShipmentForm(validFormData, {
        automaticDeparture: true,
        systemManagedInitialState: true,
      }),
    ).toBe("");

    expect(
      validateShipmentForm(
        { ...validFormData, estimatedArrival: "2026-08-26T03:59" },
        { automaticDeparture: true, systemManagedInitialState: true },
      ),
    ).toBe("Estimated arrival must be in the future.");
  });
});
