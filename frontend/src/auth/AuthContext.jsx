import { createContext, useContext, useEffect, useMemo, useState } from "react";

const AuthContext = createContext(null);

const STORAGE_KEY = "smart_auto_logistics_auth_user";

const SYSTEM_USERS = [
  {
    id: 1,
    name: "Admin User",
    email: "admin@autologistics.com",
    role: "ADMIN",
  },
  {
    id: 2,
    name: "Manufacturer User",
    email: "manufacturer@autologistics.com",
    role: "MANUFACTURER",
  },
  {
    id: 3,
    name: "Logistics Partner User",
    email: "logistics@autologistics.com",
    role: "LOGISTICS_PROVIDER",
  },
  {
    id: 4,
    name: "Dealership User",
    email: "dealer@autologistics.com",
    role: "DEALERSHIP",
  },
];

export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(null);

  useEffect(() => {
    const storedUser = localStorage.getItem(STORAGE_KEY);

    if (storedUser) {
      setCurrentUser(JSON.parse(storedUser));
    }
  }, []);

  const loginAsRole = (role) => {
    const selectedUser = SYSTEM_USERS.find((user) => user.role === role);

    if (!selectedUser) {
      throw new Error("Invalid role selected.");
    }

    localStorage.setItem(STORAGE_KEY, JSON.stringify(selectedUser));
    setCurrentUser(selectedUser);
  };

  const logout = () => {
    localStorage.removeItem(STORAGE_KEY);
    setCurrentUser(null);
  };

  const hasRole = (allowedRoles) => {
    if (!currentUser) {
      return false;
    }

    return allowedRoles.includes(currentUser.role);
  };

  const value = useMemo(
    () => ({
      currentUser,
      systemUsers: SYSTEM_USERS,
      isAuthenticated: Boolean(currentUser),
      loginAsRole,
      logout,
      hasRole,
    }),
    [currentUser],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider.");
  }

  return context;
}
