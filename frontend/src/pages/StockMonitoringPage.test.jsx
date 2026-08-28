import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, test, vi } from "vitest";
import StockMonitoringPage from "./StockMonitoringPage";

const mocks = vi.hoisted(() => ({
  currentUser: { role: "ADMIN" },
  getOverview: vi.fn(),
  createRule: vi.fn(),
  updateRule: vi.fn(),
  showToast: vi.fn(),
}));

vi.mock("../api/stockMonitoringApi", () => ({
  stockMonitoringApi: {
    getOverview: mocks.getOverview,
    createRule: mocks.createRule,
    updateRule: mocks.updateRule,
  },
}));

vi.mock("../auth/AuthContext", () => ({
  useAuth: () => ({ currentUser: mocks.currentUser }),
}));

vi.mock("../components/feedback/ToastContext", () => ({
  useToast: () => ({ showToast: mocks.showToast }),
}));

const RULE = {
  id: 11,
  ruleKey: "STK-DB-001",
  partyKey: "MFG-DB-001",
  partyRole: "MANUFACTURER",
  companyName: "Database Manufacturer",
  vehicleModelCode: "MODEL-DB-001",
  vehicleModelName: "Database Model",
  logisticsStage: "STOCK_YARD",
  minimumQuantity: 3,
  currentQuantity: 2,
  stockLevelStatus: "LOW",
  active: true,
  lastEvaluatedAt: "2026-08-18T12:00:00",
};

const ADMIN_OVERVIEW = {
  rules: [RULE],
  partyOptions: [
    { key: "MFG-DB-001", name: "Database Manufacturer", role: "MANUFACTURER" },
  ],
  vehicleModels: [
    { key: "MODEL-DB-001", brand: "Mercedes-Benz", displayName: "Database Model" },
  ],
  logisticsStageOptions: [
    { key: "STOCK_YARD", displayName: "Stock Yard" },
  ],
};

function renderPage(initialEntry = "/stock-monitoring") {
  return render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <StockMonitoringPage />
    </MemoryRouter>,
  );
}

describe("StockMonitoringPage", () => {
  beforeEach(() => {
    mocks.currentUser = { role: "ADMIN" };
    mocks.getOverview.mockResolvedValue(ADMIN_OVERVIEW);
    mocks.createRule.mockResolvedValue(RULE);
    mocks.updateRule.mockResolvedValue(RULE);
  });

  test("shows a scoped read-only view to manufacturers", async () => {
    mocks.currentUser = { role: "MANUFACTURER" };
    mocks.getOverview.mockResolvedValue({
      ...ADMIN_OVERVIEW,
      partyOptions: [],
      vehicleModels: [],
      logisticsStageOptions: [],
    });

    renderPage();

    expect(await screen.findByText("Database Manufacturer")).toBeInTheDocument();
    expect(screen.getByText("STK-DB-001")).toBeInTheDocument();
    expect(
      screen.queryByRole("heading", { name: "Add Stock Rule" }),
    ).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Edit" })).not.toBeInTheDocument();
  });

  test("keeps database-backed selects unselected until an admin chooses values", async () => {
    renderPage();

    await screen.findByRole("heading", { name: "Add Stock Rule" });

    expect(screen.getByLabelText("Company")).toHaveValue("");
    expect(screen.getByLabelText("Vehicle model")).toHaveValue("");
    expect(screen.getByLabelText("Logistics stage")).toHaveValue("");
    expect(screen.getByRole("option", { name: "Select company" })).toBeInTheDocument();
    expect(
      screen.getByRole("option", { name: "Select vehicle model" }),
    ).toBeInTheDocument();
    expect(screen.getByRole("option", { name: "Stock Yard" })).toHaveValue(
      "STOCK_YARD",
    );
  });

  test("submits selected database keys and a numeric threshold", async () => {
    const user = userEvent.setup();
    renderPage();
    await screen.findByRole("heading", { name: "Add Stock Rule" });

    await user.selectOptions(screen.getByLabelText("Company"), "MFG-DB-001");
    await user.selectOptions(
      screen.getByLabelText("Vehicle model"),
      "MODEL-DB-001",
    );
    await user.selectOptions(screen.getByLabelText("Logistics stage"), "STOCK_YARD");
    await user.type(screen.getByLabelText("Minimum quantity"), "4");
    await user.click(screen.getByRole("button", { name: "Create rule" }));

    await waitFor(() => {
      expect(mocks.createRule).toHaveBeenCalledWith({
        partyKey: "MFG-DB-001",
        vehicleModelCode: "MODEL-DB-001",
        logisticsStage: "STOCK_YARD",
        minimumQuantity: 4,
        active: true,
      });
    });
    expect(mocks.showToast).toHaveBeenCalledWith(
      expect.objectContaining({ type: "success", title: "Stock rule created" }),
    );
  });

  test("rejects a non-positive threshold before calling the API", async () => {
    renderPage();
    await screen.findByRole("heading", { name: "Add Stock Rule" });

    fireEvent.change(screen.getByLabelText("Minimum quantity"), {
      target: { value: "0" },
    });
    fireEvent.submit(screen.getByRole("button", { name: "Create rule" }).closest("form"));

    expect(
      await screen.findByText("Minimum quantity must be a whole number of at least 1."),
    ).toBeInTheDocument();
    expect(mocks.createRule).not.toHaveBeenCalled();
  });
});
