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

function normalizeAuthenticatedUser(user) {
  return {
    id: user.userId ?? user.id,
    name: user.fullName ?? user.name,
    email: user.email,
    role: user.role,
    token: user.token,
    companyName: user.companyName,
    operatingLocation: user.operatingLocation,
    locationLatitude: user.locationLatitude,
    locationLongitude: user.locationLongitude,
    partyProfileKey: user.partyProfileKey,
  };
}

function mergeAuthenticatedUser(currentUser, user) {
  return {
    ...currentUser,
    id: user.userId ?? user.id ?? currentUser?.id,
    name: user.fullName ?? user.name ?? currentUser?.name,
    email: user.email ?? currentUser?.email,
    role: user.role ?? currentUser?.role,
    token: user.token ?? currentUser?.token,
    companyName: user.companyName,
    operatingLocation: user.operatingLocation,
    locationLatitude: user.locationLatitude,
    locationLongitude: user.locationLongitude,
    partyProfileKey: user.partyProfileKey,
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

  const logout = useCallback(() => {
    clearStoredAuthUser();
    setCurrentUser(null);
  }, []);

  const updateCurrentUser = useCallback((user) => {
    setCurrentUser((previousUser) => {
      const nextUser = mergeAuthenticatedUser(previousUser, user);
      storeAuthUser(nextUser);
      return nextUser;
    });
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
      isAuthenticated: Boolean(currentUser),
      login,
      logout,
      updateCurrentUser,
      hasRole,
    }),
    [currentUser, hasRole, login, logout, updateCurrentUser],
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
