import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
} from "react";
import axiosClient from "../api/axiosClient";
import {
  clearStoredAuthUser,
  getStoredAuthUser,
  storeAuthUser,
} from "./authStorage";

const AuthContext = createContext(null);

const SYSTEM_USERS = [
  {
    id: 1,
    name: "Admin User",
    email: "admin@autologistics.com",
    password: "Admin@12345",
    role: "ADMIN",
  },
  {
    id: 2,
    name: "Manufacturer User",
    email: "manufacturer@autologistics.com",
    password: "Manufacturer@12345",
    role: "MANUFACTURER",
  },
  {
    id: 3,
    name: "Logistics Partner User",
    email: "logistics@autologistics.com",
    password: "Logistics@12345",
    role: "LOGISTICS_PROVIDER",
  },
  {
    id: 4,
    name: "Dealership User",
    email: "dealer@autologistics.com",
    password: "Dealer@12345",
    role: "DEALERSHIP",
  },
];

function normalizeAuthenticatedUser(user) {
  return {
    id: user.userId ?? user.id,
    name: user.fullName ?? user.name,
    email: user.email,
    role: user.role,
    token: user.token,
  };
}

export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(getStoredAuthUser);

  const login = useCallback(async ({ email, password }) => {
    const response = await axiosClient.post("/auth/login", {
      email,
      password,
    });

    const authenticatedUser = normalizeAuthenticatedUser(response.data);

    storeAuthUser(authenticatedUser);
    setCurrentUser(authenticatedUser);

    return authenticatedUser;
  }, []);

  const loginAsRole = useCallback(
    async (role) => {
      const selectedUser = SYSTEM_USERS.find((user) => user.role === role);

      if (!selectedUser) {
        throw new Error("Invalid role selected.");
      }

      return login({
        email: selectedUser.email,
        password: selectedUser.password,
      });
    },
    [login],
  );

  const logout = useCallback(() => {
    clearStoredAuthUser();
    setCurrentUser(null);
  }, []);

  const hasRole = useCallback(
    (allowedRoles) => {
      if (!currentUser) {
        return false;
      }

      return allowedRoles.includes(currentUser.role);
    },
    [currentUser],
  );

  const value = useMemo(
    () => ({
      currentUser,
      systemUsers: SYSTEM_USERS,
      isAuthenticated: Boolean(currentUser),
      login,
      loginAsRole,
      logout,
      hasRole,
    }),
    [currentUser, hasRole, login, loginAsRole, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider.");
  }

  return context;
}
