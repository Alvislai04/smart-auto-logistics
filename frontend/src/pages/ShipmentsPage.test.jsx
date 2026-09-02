import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, test, vi } from "vitest";
import ShipmentsPage from "./ShipmentsPage";

const mocks = vi.hoisted(() => ({
  currentUser: { role: "LOGISTICS_PROVIDER" },
  getAll: vi.fn(),
  updateStatus: vi.fn(),
  showToast: vi.fn(),
}));

vi.mock("../api/shipmentApi", () => ({
  shipmentApi: {
    getAll: mocks.getAll,
    updateStatus: mocks.updateStatus,
  },
}));

vi.mock("../auth/AuthContext", () => ({
  useAuth: () => ({ currentUser: mocks.currentUser }),
}));

vi.mock("../components/feedback/ToastContext", () => ({
  useToast: () => ({ showToast: mocks.showToast }),
}));

const TRANSIT_SHIPMENT = {
  id: 42,
  shipmentCode: "SHP-TEST-042",
  vehicleModel: "Mercedes-Benz C-Class",
  vehicleVin: "WDDTEST0000000042",
  origin: "Pekan Manufacturing Plant",
  destination: "Kuala Lumpur Dealership",
  currentStage: "IN_TRANSIT",
  status: "IN_TRANSIT",
  estimatedDeparture: "2026-09-02T09:00:00",
  estimatedArrival: "2026-09-02T12:00:00",
  responsiblePerson: "Logistics Operator",
  stockLocation: null,
};

function pageWith(shipment) {
  return {
    content: [shipment],
    page: 0,
    size: 10,
    totalPages: 1,
    totalElements: 1,
    numberOfElements: 1,
    first: true,
    last: true,
  };
}

describe("ShipmentsPage logistics arrival action", () => {
  beforeEach(() => {
    mocks.currentUser = { role: "LOGISTICS_PROVIDER" };
    mocks.getAll.mockReset();
    mocks.updateStatus.mockReset();
    mocks.showToast.mockReset();
    mocks.getAll.mockResolvedValue(pageWith(TRANSIT_SHIPMENT));
    mocks.updateStatus.mockResolvedValue({
      ...TRANSIT_SHIPMENT,
      status: "ARRIVED",
      currentStage: "ARRIVED_AT_DEALERSHIP",
    });
  });

  test("marks an in-transit shipment as arrived from the shipment list", async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <ShipmentsPage />
      </MemoryRouter>,
    );

    await user.click(await screen.findByRole("button", { name: "Mark Arrived" }));

    await waitFor(() =>
      expect(mocks.updateStatus).toHaveBeenCalledWith(42, "ARRIVED"),
    );
    expect(mocks.showToast).toHaveBeenCalledWith(
      expect.objectContaining({ type: "success", title: "Arrival confirmed" }),
    );
  });

  test("does not expose the arrival action to a dealership", async () => {
    mocks.currentUser = { role: "DEALERSHIP" };

    render(
      <MemoryRouter>
        <ShipmentsPage />
      </MemoryRouter>,
    );

    await screen.findByText("SHP-TEST-042");
    expect(
      screen.queryByRole("button", { name: "Mark Arrived" }),
    ).not.toBeInTheDocument();
  });
});
