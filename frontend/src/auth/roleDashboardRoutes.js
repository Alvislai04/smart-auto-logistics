export const ROLE_DASHBOARD_ROUTES = {
  ADMIN: "/admin/dashboard",
  MANUFACTURER: "/manufacturer/dashboard",
  LOGISTICS_PROVIDER: "/logistics/dashboard",
  DEALERSHIP: "/dealership/dashboard",
};

export function getDashboardPathForRole(role) {
  return ROLE_DASHBOARD_ROUTES[role] ?? "/access-denied";
}
