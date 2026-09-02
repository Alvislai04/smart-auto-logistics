import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { hasAvailableShipmentStatusActions } from "../../utils/shipmentStatusActions";
import ShipmentStatusActions from "./ShipmentStatusActions";

describe("ShipmentStatusActions", () => {
  it("allows logistics providers to start or cancel a created shipment", async () => {
    const user = userEvent.setup();
    const onUpdateStatus = vi.fn();

    render(
      <ShipmentStatusActions
        currentStatus="CREATED"
        currentUserRole="LOGISTICS_PROVIDER"
        isUpdating={false}
        onUpdateStatus={onUpdateStatus}
      />,
    );

    await user.click(screen.getByRole("button", { name: "Start Transit" }));
    await user.click(screen.getByRole("button", { name: "Cancel" }));

    expect(onUpdateStatus).toHaveBeenNthCalledWith(1, "IN_TRANSIT");
    expect(onUpdateStatus).toHaveBeenNthCalledWith(2, "CANCELLED");
    expect(
      screen.queryByRole("button", { name: "Mark Arrived" }),
    ).not.toBeInTheDocument();
  });

  it("does not expose workflow actions to manufacturers", () => {
    expect(hasAvailableShipmentStatusActions("CREATED", "MANUFACTURER")).toBe(
      false,
    );

    render(
      <ShipmentStatusActions
        currentStatus="CREATED"
        currentUserRole="MANUFACTURER"
        isUpdating={false}
        onUpdateStatus={vi.fn()}
      />,
    );

    expect(
      screen.queryByRole("button", { name: "Start Transit" }),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Cancel" }),
    ).not.toBeInTheDocument();
  });

  it("allows logistics providers to confirm arrival or cancel during transit", async () => {
    const user = userEvent.setup();
    const onUpdateStatus = vi.fn();

    render(
      <ShipmentStatusActions
        currentStatus="IN_TRANSIT"
        currentUserRole="LOGISTICS_PROVIDER"
        isUpdating={false}
        onUpdateStatus={onUpdateStatus}
      />,
    );

    await user.click(screen.getByRole("button", { name: "Mark Arrived" }));

    expect(onUpdateStatus).toHaveBeenCalledWith("ARRIVED");
    expect(screen.getByRole("button", { name: "Cancel" })).toBeVisible();
    expect(
      screen.queryByRole("button", { name: "Start Transit" }),
    ).not.toBeInTheDocument();
  });

  it("allows a delayed shipment to be marked as arrived", () => {
    expect(
      hasAvailableShipmentStatusActions("DELAYED", "LOGISTICS_PROVIDER"),
    ).toBe(true);

    render(
      <ShipmentStatusActions
        currentStatus="DELAYED"
        currentUserRole="LOGISTICS_PROVIDER"
        isUpdating={false}
        onUpdateStatus={vi.fn()}
      />,
    );

    expect(screen.getByRole("button", { name: "Mark Arrived" })).toBeVisible();
  });

  it("does not expose any logistics action after confirmed arrival", () => {
    expect(
      hasAvailableShipmentStatusActions("ARRIVED", "LOGISTICS_PROVIDER"),
    ).toBe(false);

    render(
      <ShipmentStatusActions
        currentStatus="ARRIVED"
        currentUserRole="LOGISTICS_PROVIDER"
        isUpdating={false}
        onUpdateStatus={vi.fn()}
      />,
    );

    expect(screen.queryByRole("button")).not.toBeInTheDocument();
  });
});
