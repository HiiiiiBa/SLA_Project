"use client";

import { useAuth } from "@/context/AuthContext";

/** Current logged-in user id — include in data-fetch effect deps so pages reload per account. */
export function useSessionUserId(): number | undefined {
  const { user } = useAuth();
  return user?.userId;
}
