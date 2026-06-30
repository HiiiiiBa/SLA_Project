"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { useRouter } from "next/navigation";
import { login as apiLogin, logout as apiLogout } from "@/lib/api";
import { getStoredAuth } from "@/lib/auth-storage";
import type { AuthUser, LoginRequest } from "@/types";

interface AuthContextValue {
  user: AuthUser | null;
  loading: boolean;
  login: (request: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  isAdmin: boolean;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setUser(getStoredAuth());
    setLoading(false);
  }, []);

  const login = useCallback(
    async (request: LoginRequest) => {
      const auth = await apiLogin(request);
      setUser(auth);
      router.push("/dashboard");
    },
    [router],
  );

  const logout = useCallback(async () => {
    await apiLogout();
    setUser(null);
    router.push("/login");
  }, [router]);

  const value = useMemo(
    () => ({
      user,
      loading,
      login,
      logout,
      isAdmin: user?.role === "ADMIN",
    }),
    [user, loading, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
