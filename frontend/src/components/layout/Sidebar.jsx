import { NavLink } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { getDashboardPathForRole } from "../../auth/roleDashboardRoutes";

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
    path: "/alerts",
    label: "Alerts",
    roles: ["ADMIN", "LOGISTICS_PROVIDER", "DEALERSHIP"],
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
  const dashboardPath = getDashboardPathForRole(currentUser?.role);

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
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}

export default Sidebar;
