import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, test, vi } from "vitest";
import AlertsPage from "./AlertsPage";

const mocks = vi.hoisted(() => ({
  currentUser: { role: "MANUFACTURER" },
  getOpenAlerts: vi.fn(),
  acknowledge: vi.fn(),
  resolve: vi.fn(),
  showToast: vi.fn(),
}));

vi.mock("../api/alertApi", () => ({
  alertApi: {
    getOpenAlerts: mocks.getOpenAlerts,
    acknowledge: mocks.acknowledge,
    resolve: mocks.resolve,
  },
}));

vi.mock("../auth/AuthContext", () => ({
  useAuth: () => ({ currentUser: mocks.currentUser }),
}));

vi.mock("../components/feedback/ToastContext", () => ({
  useToast: () => ({ showToast: mocks.showToast }),
}));

const LOW_STOCK_ALERT = {
  id: 51,
  alertType: "LOW_STOCK",
  severity: "HIGH",
  title: "Low stock detected",
  message: "Current quantity is below the configured minimum.",
  referenceType: "STOCK_RULE",
  referenceId: 11,
  referenceLabel: "STK-DB-001",
  createdAt: "2026-08-18T12:00:00",
};

const DELAY_ALERT = {
  id: 52,
  alertType: "DELAY",
  severity: "MEDIUM",
  title: "Shipment delayed",
  message: "The shipment has fallen behind schedule.",
  referenceType: "SHIPMENT",
  referenceId: 7,
  referenceLabel: "SHP-DB-001",
  createdAt: "2026-08-18T12:05:00",
};

function renderPage() {
  return render(
    <MemoryRouter>
      <AlertsPage />
    </MemoryRouter>,
  );
}

describe("AlertsPage", () => {
  beforeEach(() => {
    mocks.currentUser = { role: "MANUFACTURER" };
    mocks.getOpenAlerts.mockResolvedValue([LOW_STOCK_ALERT, DELAY_ALERT]);
    mocks.acknowledge.mockResolvedValue(LOW_STOCK_ALERT);
    mocks.resolve.mockResolvedValue(LOW_STOCK_ALERT);
  });

  test("links each alert to its correct reference type", async () => {
    renderPage();

    expect(await screen.findByText("Low stock detected")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "STK-DB-001" })).toHaveAttribute(
      "href",
      "/stock-monitoring?rule=11",
    );
    expect(screen.getByRole("link", { name: "SHP-DB-001" })).toHaveAttribute(
      "href",
      "/shipments/7",
    );
  });

  test("limits manufacturer actions to supported alert acknowledgements", async () => {
    const user = userEvent.setup();
    renderPage();
    await screen.findByText("Low stock detected");

    expect(screen.getAllByRole("button", { name: "Acknowledge" })).toHaveLength(1);
    expect(screen.queryByRole("button", { name: "Resolve" })).not.toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Acknowledge" }));

    await waitFor(() => expect(mocks.acknowledge).toHaveBeenCalledWith(51));
    expect(mocks.showToast).toHaveBeenCalledWith(
      expect.objectContaining({ type: "success", title: "Alert acknowledged" }),
    );
  });

  test("allows administrators to resolve an alert", async () => {
    const user = userEvent.setup();
    mocks.currentUser = { role: "ADMIN" };
    mocks.getOpenAlerts.mockResolvedValue([LOW_STOCK_ALERT]);
    renderPage();
    await screen.findByText("Low stock detected");

    await user.click(screen.getByRole("button", { name: "Resolve" }));

    await waitFor(() => expect(mocks.resolve).toHaveBeenCalledWith(51));
  });
});
