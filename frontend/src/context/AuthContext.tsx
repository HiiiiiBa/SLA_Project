"use client";

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { login as apiLogin, logout as apiLogout } from "@/lib/api";
import { getStoredAuth } from "@/lib/auth-storage";
import type { AuthUser, LoginRequest } from "@/types";

interface AuthContextValue {
  user: AuthUser | null;
  loading: boolean;
  login: (request: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  isAdmin: boolean;
  isManager: boolean;
  isEmployee: boolean;
  isClient: boolean;
  hasGlobalDashboard: boolean;
  canManageOrg: boolean;
  canManageSla: boolean;
  canManageSlaLifecycle: boolean;
  canApproveRequests: boolean;
  canRequestApproval: boolean;
  canViewClients: boolean;
  canCreateIncident: boolean;
  canModifyIncident: boolean;
  canAssignIncident: boolean;
  canGenerateReports: boolean;
  canDownloadReports: boolean;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setUser(getStoredAuth());
    setLoading(false);
  }, []);

  const login = useCallback(async (request: LoginRequest) => {
    const auth = await apiLogin(request);
    setUser(auth);
    window.location.assign("/dashboard");
  }, []);

  const logout = useCallback(async () => {
    await apiLogout();
    setUser(null);
    window.location.assign("/login");
  }, []);

  const value = useMemo(() => {
    const role = user?.role;
    const isAdmin = role === "ADMIN";
    const isManager = role === "MANAGER";
    const isEmployee = role === "EMPLOYEE";
    const isClient = role === "CLIENT";

    return {
      user,
      loading,
      login,
      logout,
      isAdmin,
      isManager,
      isEmployee,
      isClient,
      hasGlobalDashboard: isAdmin,
      canManageOrg: isAdmin || isManager,
      canManageSla: isAdmin || isManager,
      canManageSlaLifecycle: isAdmin,
      canApproveRequests: isAdmin,
      canRequestApproval: isManager,
      canViewClients: isAdmin || isManager,
      canCreateIncident: isAdmin || isManager || isClient,
      canModifyIncident: isAdmin || isManager || isEmployee,
      canAssignIncident: isAdmin || isManager,
      canGenerateReports: isAdmin || isManager,
      canDownloadReports: isAdmin || isManager || isClient,
    };
  }, [user, loading, login, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}

