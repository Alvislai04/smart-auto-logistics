import { Navigate, Route, Routes } from "react-router-dom";

import Sidebar from "./components/layout/Sidebar";
import Topbar from "./components/layout/Topbar";

import ProtectedRoute from "./auth/ProtectedRoute";
import RoleDashboardRedirect from "./auth/RoleDashboardRedirect";

import DashboardPage from "./pages/DashboardPage";
import ShipmentsPage from "./pages/ShipmentsPage";
import CreateShipmentPage from "./pages/CreateShipmentPage";
import ShipmentDetailsPage from "./pages/ShipmentDetailsPage";
import AlertsPage from "./pages/AlertsPage";
import AccountSettingsPage from "./pages/AccountSettingsPage";
import SimulationSessionsPage from "./pages/SimulationSessionsPage";
import ReportsPage from "./pages/reports/ReportsPage";

import BlockchainMonitorPage from "./pages/admin/BlockchainMonitorPage";
import UserAccountsPage from "./pages/admin/UserAccountsPage";
import AdminNotificationsPage from "./pages/admin/AdminNotificationsPage";
import CompanyNotificationsPage from "./pages/admin/CompanyNotificationsPage";
import IntegrityCasesPage from "./pages/admin/IntegrityCasesPage";

import LoginPage from "./pages/auth/LoginPage";
import AccessDeniedPage from "./pages/auth/AccessDeniedPage";

const ALL_ROLES = ["ADMIN", "MANUFACTURER", "LOGISTICS_PROVIDER", "DEALERSHIP"];

const ADMIN_ONLY = ["ADMIN"];

function AppLayout({ children }) {
  return (
    <div className="app-layout">
      <Sidebar />

      <main className="main-content">
        <Topbar />
        {children}
      </main>
    </div>
  );
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route
        path="/access-denied"
        element={
          <ProtectedRoute allowedRoles={ALL_ROLES}>
            <AppLayout>
              <AccessDeniedPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/"
        element={
          <ProtectedRoute allowedRoles={ALL_ROLES}>
            <AppLayout>
              <RoleDashboardRedirect />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/admin/dashboard"
        element={
          <ProtectedRoute allowedRoles={["ADMIN"]}>
            <AppLayout>
              <DashboardPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/manufacturer/dashboard"
        element={
          <ProtectedRoute allowedRoles={["MANUFACTURER"]}>
            <AppLayout>
              <DashboardPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/logistics/dashboard"
        element={
          <ProtectedRoute allowedRoles={["LOGISTICS_PROVIDER"]}>
            <AppLayout>
              <DashboardPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/dealership/dashboard"
        element={
          <ProtectedRoute allowedRoles={["DEALERSHIP"]}>
            <AppLayout>
              <DashboardPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/shipments"
        element={
          <ProtectedRoute allowedRoles={ALL_ROLES}>
            <AppLayout>
              <ShipmentsPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/shipments/create"
        element={
          <ProtectedRoute allowedRoles={["MANUFACTURER"]}>
            <AppLayout>
              <CreateShipmentPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/shipments/:shipmentId"
        element={
          <ProtectedRoute allowedRoles={ALL_ROLES}>
            <AppLayout>
              <ShipmentDetailsPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/alerts"
        element={
          <ProtectedRoute
            allowedRoles={["ADMIN", "LOGISTICS_PROVIDER", "DEALERSHIP"]}
          >
            <AppLayout>
              <AlertsPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/simulation"
        element={
          <ProtectedRoute allowedRoles={["ADMIN", "LOGISTICS_PROVIDER"]}>
            <AppLayout>
              <SimulationSessionsPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/reports"
        element={
          <ProtectedRoute allowedRoles={ALL_ROLES}>
            <AppLayout>
              <ReportsPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/account"
        element={
          <ProtectedRoute allowedRoles={ALL_ROLES}>
            <AppLayout>
              <AccountSettingsPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/admin/users"
        element={
          <ProtectedRoute allowedRoles={ADMIN_ONLY}>
            <AppLayout>
              <UserAccountsPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/admin/blockchain-monitor"
        element={
          <ProtectedRoute allowedRoles={ADMIN_ONLY}>
            <AppLayout>
              <BlockchainMonitorPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/admin/notifications"
        element={
          <ProtectedRoute allowedRoles={ADMIN_ONLY}>
            <AppLayout>
              <AdminNotificationsPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/admin/company-notifications"
        element={
          <ProtectedRoute allowedRoles={ADMIN_ONLY}>
            <AppLayout>
              <CompanyNotificationsPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/admin/integrity-cases"
        element={
          <ProtectedRoute allowedRoles={ADMIN_ONLY}>
            <AppLayout>
              <IntegrityCasesPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
