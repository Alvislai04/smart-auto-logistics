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
        isUpdating={false}
        onUpdateStatus={vi.fn()}
      />,
    );

    expect(
      screen.queryByRole("button", { name: "Mark Delivered" }),
    ).not.toBeInTheDocument();
  });
});
