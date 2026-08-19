import { useCallback, useEffect, useState } from "react";
import { NavLink } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { getDashboardPathForRole } from "../../auth/roleDashboardRoutes";
import { alertApi } from "../../api/alertApi";
import {
  alertBadgeRefreshIntervalMs,
  alertCountRefreshEventName,
} from "../../config/alertConfig";

const navItems = [
  {
    key: "dashboard",
    path: "/",
    label: "Dashboard",
    roles: ["ADMIN", "MANUFACTURER", "LOGISTICS_PROVIDER", "DEALERSHIP"],
  },
  {
    path: "/shipments",
    label: "Shipments",
    roles: ["ADMIN", "MANUFACTURER", "LOGISTICS_PROVIDER", "DEALERSHIP"],
  },
  {
    key: "alerts",
    path: "/alerts",
    label: "Alerts",
    roles: ["ADMIN", "MANUFACTURER", "LOGISTICS_PROVIDER", "DEALERSHIP"],
  },
  {
    path: "/stock-monitoring",
    label: "Stock Monitoring",
    roles: ["ADMIN", "MANUFACTURER", "DEALERSHIP"],
  },
  {
    path: "/simulation",
    label: "Simulation",
    roles: ["ADMIN", "LOGISTICS_PROVIDER"],
  },
  {
    path: "/reports",
    label: "Reports",
    roles: ["ADMIN", "MANUFACTURER", "LOGISTICS_PROVIDER", "DEALERSHIP"],
  },
  {
    path: "/account",
    label: "My Account",
    roles: ["ADMIN", "MANUFACTURER", "LOGISTICS_PROVIDER", "DEALERSHIP"],
  },
  {
    path: "/admin/users",
    label: "User Accounts",
    roles: ["ADMIN"],
  },
  {
    path: "/admin/master-data",
    label: "Master Data",
    roles: ["ADMIN"],
  },
  {
    path: "/admin/blockchain-monitor",
    label: "Blockchain Monitor",
    roles: ["ADMIN"],
  },
  {
    path: "/admin/notifications",
    label: "Admin Alerts",
    roles: ["ADMIN"],
  },
  {
    path: "/admin/company-notifications",
    label: "Company Notices",
    roles: ["ADMIN"],
  },
  {
    path: "/admin/integrity-cases",
    label: "Integrity Cases",
    roles: ["ADMIN"],
  },
];

function Sidebar() {
  const { currentUser } = useAuth();
  const [openAlertCount, setOpenAlertCount] = useState(0);
  const dashboardPath = getDashboardPathForRole(currentUser?.role);

  const loadOpenAlertCount = useCallback(async () => {
    if (!currentUser) {
      setOpenAlertCount(0);
      return;
    }

    try {
      const data = await alertApi.getOpenAlertCount();
      setOpenAlertCount(Number(data?.openAlerts ?? 0));
    } catch {
      setOpenAlertCount(0);
    }
  }, [currentUser]);

  useEffect(() => {
    const refreshWhenVisible = () => {
      if (document.visibilityState !== "hidden") {
        loadOpenAlertCount();
      }
    };

    refreshWhenVisible();

    const intervalId = window.setInterval(
      refreshWhenVisible,
      alertBadgeRefreshIntervalMs,
    );

    window.addEventListener(alertCountRefreshEventName, refreshWhenVisible);
    document.addEventListener("visibilitychange", refreshWhenVisible);

    return () => {
      window.clearInterval(intervalId);
      window.removeEventListener(alertCountRefreshEventName, refreshWhenVisible);
      document.removeEventListener("visibilitychange", refreshWhenVisible);
    };
  }, [loadOpenAlertCount]);

  const visibleItems = navItems
    .filter((item) => item.roles.includes(currentUser?.role))
    .map((item) =>
      item.key === "dashboard" ? { ...item, path: dashboardPath } : item,
    );

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <h2>Auto Logistics</h2>
        <span>{currentUser?.role?.replaceAll("_", " ")}</span>
      </div>

      <nav className="sidebar-nav">
        {visibleItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) =>
              isActive ? "sidebar-link active" : "sidebar-link"
            }
          >
            <span className="sidebar-link-label">{item.label}</span>
            {item.key === "alerts" && openAlertCount > 0 && (
              <span
                className="sidebar-badge"
                aria-label={`${openAlertCount} open alerts`}
              >
                {openAlertCount > 99 ? "99+" : openAlertCount}
              </span>
            )}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}

export default Sidebar;
