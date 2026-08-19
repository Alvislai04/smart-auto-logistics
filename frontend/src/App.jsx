import { lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";

import Sidebar from "./components/layout/Sidebar";
import Topbar from "./components/layout/Topbar";

import ProtectedRoute from "./auth/ProtectedRoute";
import RoleDashboardRedirect from "./auth/RoleDashboardRedirect";

const DashboardPage = lazy(() => import("./pages/DashboardPage"));
const ShipmentsPage = lazy(() => import("./pages/ShipmentsPage"));
const CreateShipmentPage = lazy(() => import("./pages/CreateShipmentPage"));
const EditShipmentPage = lazy(() => import("./pages/EditShipmentPage"));
const ShipmentDetailsPage = lazy(() => import("./pages/ShipmentDetailsPage"));
const AlertsPage = lazy(() => import("./pages/AlertsPage"));
const StockMonitoringPage = lazy(() => import("./pages/StockMonitoringPage"));
const AccountSettingsPage = lazy(() => import("./pages/AccountSettingsPage"));
const SimulationSessionsPage = lazy(() => import("./pages/SimulationSessionsPage"));
const ReportsPage = lazy(() => import("./pages/reports/ReportsPage"));

const BlockchainMonitorPage = lazy(
  () => import("./pages/admin/BlockchainMonitorPage"),
);
const MasterDataPage = lazy(() => import("./pages/admin/MasterDataPage"));
const UserAccountsPage = lazy(() => import("./pages/admin/UserAccountsPage"));
const AdminNotificationsPage = lazy(
  () => import("./pages/admin/AdminNotificationsPage"),
);
const CompanyNotificationsPage = lazy(
  () => import("./pages/admin/CompanyNotificationsPage"),
);
const IntegrityCasesPage = lazy(
  () => import("./pages/admin/IntegrityCasesPage"),
);

const LoginPage = lazy(() => import("./pages/auth/LoginPage"));
const AccessDeniedPage = lazy(() => import("./pages/auth/AccessDeniedPage"));

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
    <Suspense
      fallback={
        <div className="route-loading" role="status" aria-live="polite">
          <span className="route-loading-spinner" aria-hidden="true" />
          <span>Loading page...</span>
        </div>
      }
    >
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
        path="/shipments/:shipmentId/edit"
        element={
          <ProtectedRoute allowedRoles={["ADMIN", "MANUFACTURER"]}>
            <AppLayout>
              <EditShipmentPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/alerts"
        element={
          <ProtectedRoute allowedRoles={ALL_ROLES}>
            <AppLayout>
              <AlertsPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/stock-monitoring"
        element={
          <ProtectedRoute allowedRoles={["ADMIN", "MANUFACTURER", "DEALERSHIP"]}>
            <AppLayout>
              <StockMonitoringPage />
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
        path="/admin/master-data"
        element={
          <ProtectedRoute allowedRoles={ADMIN_ONLY}>
            <AppLayout>
              <MasterDataPage />
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
    </Suspense>
  );
}

export default App;
