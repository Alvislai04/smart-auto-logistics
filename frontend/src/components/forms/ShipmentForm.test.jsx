import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import {
  emptyShipmentFormOptions,
  initialShipmentFormData,
} from "../../utils/shipmentFormUtils";
import ShipmentForm from "./ShipmentForm";

describe("ShipmentForm creation mode", () => {
  it("hides system-managed state fields and limits estimated arrival", () => {
    render(
      <ShipmentForm
        formData={initialShipmentFormData}
        formOptions={emptyShipmentFormOptions}
        isDepartureAutomatic
        isInitialStateSystemManaged
        minimumEstimatedArrival="2026-09-02T09:30"
        isLoadingOptions={false}
        isSubmitting={false}
        submitButtonLabel="Create Shipment"
        onChange={vi.fn()}
        onSubmit={vi.fn()}
      />,
    );

    expect(
      screen.queryByLabelText("Current Logistics Stage"),
    ).not.toBeInTheDocument();
    expect(screen.queryByLabelText("WIP Status")).not.toBeInTheDocument();
    expect(screen.getByLabelText("Estimated Arrival")).toHaveAttribute(
      "min",
      "2026-09-02T09:30",
    );
  });
});
