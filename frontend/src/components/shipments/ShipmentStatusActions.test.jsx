import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import ShipmentStatusActions from "./ShipmentStatusActions";

describe("ShipmentStatusActions", () => {
  it("allows logistics workflow arrival from in-transit status", async () => {
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
  });

  it("does not expose a manual delivered action after arrival", () => {
    render(
      <ShipmentStatusActions
        currentStatus="ARRIVED"
        currentUserRole="LOGISTICS_PROVIDER"
        isUpdating={false}
        onUpdateStatus={vi.fn()}
      />,
    );

    expect(
      screen.queryByRole("button", { name: "Mark Delivered" }),
    ).not.toBeInTheDocument();
  });

  it("allows only manufacturers to start transit", async () => {
    const user = userEvent.setup();
    const onUpdateStatus = vi.fn();
    const { rerender } = render(
      <ShipmentStatusActions
        currentStatus="CREATED"
        currentUserRole="LOGISTICS_PROVIDER"
        isUpdating={false}
        onUpdateStatus={onUpdateStatus}
      />,
    );

    expect(
      screen.queryByRole("button", { name: "Start Transit" }),
    ).not.toBeInTheDocument();

    rerender(
      <ShipmentStatusActions
        currentStatus="CREATED"
        currentUserRole="MANUFACTURER"
        isUpdating={false}
        onUpdateStatus={onUpdateStatus}
      />,
    );

    await user.click(screen.getByRole("button", { name: "Start Transit" }));
    expect(onUpdateStatus).toHaveBeenCalledWith("IN_TRANSIT");
  });

  it("limits logistics providers to arrival and cancellation actions", () => {
    render(
      <ShipmentStatusActions
        currentStatus="IN_TRANSIT"
        currentUserRole="LOGISTICS_PROVIDER"
        isUpdating={false}
        onUpdateStatus={vi.fn()}
      />,
    );

    expect(screen.getByRole("button", { name: "Mark Arrived" })).toBeVisible();
    expect(screen.getByRole("button", { name: "Cancel" })).toBeVisible();
    expect(
      screen.queryByRole("button", { name: "Start Transit" }),
    ).not.toBeInTheDocument();
  });
});
