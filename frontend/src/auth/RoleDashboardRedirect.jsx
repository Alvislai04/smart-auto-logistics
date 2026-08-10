import { Navigate } from "react-router-dom";
import { useAuth } from "./AuthContext";
import { getDashboardPathForRole } from "./roleDashboardRoutes";

function RoleDashboardRedirect() {
  const { currentUser } = useAuth();

  return (
    <Navigate
      to={getDashboardPathForRole(currentUser?.role)}
      replace
    />
  );
}

export default RoleDashboardRedirect;
