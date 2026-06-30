import type { AuthUser } from "@/types";

const AUTH_KEY = "sla_auth";

export function getStoredAuth(): AuthUser | null {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem(AUTH_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    return null;
  }
}

export function storeAuth(auth: AuthUser) {
  localStorage.setItem(AUTH_KEY, JSON.stringify(auth));
}

export function clearAuth() {
  localStorage.removeItem(AUTH_KEY);
}
