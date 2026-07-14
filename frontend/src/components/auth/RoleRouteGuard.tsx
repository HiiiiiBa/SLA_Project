"use client";

import { useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";

const ALL_FORBIDDEN_PREFIXES = ["/notifications", "/services"];
const CLIENT_FORBIDDEN_PREFIXES = ["/admin", "/clients", "/teams", "/services", "/maintenance"];
const EMPLOYEE_FORBIDDEN_PREFIXES = ["/admin", "/clients", "/teams", "/services"];
const MANAGER_FORBIDDEN_PREFIXES = ["/services"];

function isForbiddenPath(pathname: string, prefixes: string[]) {
  return prefixes.some(
    (prefix) => pathname === prefix || pathname.startsWith(`${prefix}/`),
  );
}

export function RoleRouteGuard({ children }: { children: React.ReactNode }) {
  const { user, isClient, isEmployee, isManager, loading } = useAuth();
  const pathname = usePathname();
  const router = useRouter();
  const forbidden =
    Boolean(user && isForbiddenPath(pathname, ALL_FORBIDDEN_PREFIXES))
    || (isClient && isForbiddenPath(pathname, CLIENT_FORBIDDEN_PREFIXES))
    || (isEmployee && isForbiddenPath(pathname, EMPLOYEE_FORBIDDEN_PREFIXES))
    || (isManager && isForbiddenPath(pathname, MANAGER_FORBIDDEN_PREFIXES));

  useEffect(() => {
    if (!loading && forbidden) {
      router.replace("/dashboard");
    }
  }, [loading, forbidden, router]);

  if (loading || forbidden) {
    return null;
  }

  return <>{children}</>;
}
