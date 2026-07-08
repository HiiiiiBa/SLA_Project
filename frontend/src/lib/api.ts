import { clearAuth, getStoredAuth, storeAuth } from "@/lib/auth-storage";
import type { ApiResponse, AuthUser, LoginRequest } from "@/types";

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
  ) {
    super(message);
  }
}

async function refreshAccessToken(): Promise<AuthUser | null> {
  const auth = getStoredAuth();
  if (!auth?.refreshToken) return null;

  const response = await fetch(`${API_URL}/api/auth/refresh`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken: auth.refreshToken }),
  });

  if (!response.ok) {
    clearAuth();
    return null;
  }

  const payload = (await response.json()) as ApiResponse<AuthUser>;
  const updated: AuthUser = {
    userId: payload.data.userId,
    email: payload.data.email,
    role: payload.data.role,
    accessToken: payload.data.accessToken,
    refreshToken: payload.data.refreshToken,
    expiresIn: payload.data.expiresIn,
  };
  storeAuth(updated);
  return updated;
}

export async function apiFetch<T>(
  path: string,
  options: RequestInit = {},
  retry = true,
): Promise<T> {
  const auth = getStoredAuth();
  const headers = new Headers(options.headers);

  if (!headers.has("Content-Type") && !(options.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }

  if (auth?.accessToken) {
    headers.set("Authorization", `Bearer ${auth.accessToken}`);
  }

  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers,
  });

  if (response.status === 401 && retry && auth?.refreshToken) {
    const refreshed = await refreshAccessToken();
    if (refreshed) {
      return apiFetch<T>(path, options, false);
    }
    throw new ApiError("Session expirée", 401);
  }

  if (!response.ok) {
    let message = "Une erreur est survenue";
    try {
      const errorBody = (await response.json()) as ApiResponse<unknown>;
      message = errorBody.message || message;
    } catch {
      message = response.statusText || message;
    }
    throw new ApiError(message, response.status);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const contentType = response.headers.get("content-type") ?? "";
  if (!contentType.includes("application/json")) {
    return response as unknown as T;
  }

  const payload = (await response.json()) as ApiResponse<T>;
  return payload.data;
}

export async function login(request: LoginRequest): Promise<AuthUser> {
  const response = await fetch(`${API_URL}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    let message = "Identifiants invalides";
    try {
      const errorBody = (await response.json()) as ApiResponse<unknown>;
      message = errorBody.message || message;
    } catch {
      /* ignore */
    }
    throw new ApiError(message, response.status);
  }

  const payload = (await response.json()) as ApiResponse<AuthUser>;
  const auth: AuthUser = {
    userId: payload.data.userId,
    email: payload.data.email,
    role: payload.data.role,
    accessToken: payload.data.accessToken,
    refreshToken: payload.data.refreshToken,
    expiresIn: payload.data.expiresIn,
  };
  storeAuth(auth);
  return auth;
}

export async function logout() {
  const auth = getStoredAuth();
  if (auth?.refreshToken) {
    try {
      await apiFetch<void>("/api/auth/logout", {
        method: "POST",
        body: JSON.stringify({ refreshToken: auth.refreshToken }),
      });
    } catch {
      /* ignore logout errors */
    }
  }
  clearAuth();
}

export async function downloadReport(reportId: number, format: "pdf" | "csv") {
  const auth = getStoredAuth();
  const response = await fetch(
    `${API_URL}/api/reports/${reportId}/export/${format}`,
    {
      headers: auth?.accessToken
        ? { Authorization: `Bearer ${auth.accessToken}` }
        : {},
    },
  );

  if (!response.ok) {
    throw new ApiError("Échec du téléchargement", response.status);
  }

  const blob = await response.blob();
  const disposition = response.headers.get("content-disposition") ?? "";
  const match = disposition.match(/filename="?([^"]+)"?/);
  const filename = match?.[1] ?? `sla-report-${reportId}.${format}`;

  const url = window.URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}

async function downloadBlobResponse(response: Response, fallbackName: string) {
  if (!response.ok) {
    let message = "Échec de l'export PDF";
    try {
      const errorBody = (await response.json()) as ApiResponse<unknown>;
      message = errorBody.message || message;
    } catch {
      /* ignore */
    }
    throw new ApiError(message, response.status);
  }

  const blob = await response.blob();
  const disposition = response.headers.get("content-disposition") ?? "";
  const match = disposition.match(/filename="?([^"]+)"?/);
  const filename = match?.[1] ?? fallbackName;

  const url = window.URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}

export async function downloadExecutiveReportPdf(report: unknown) {
  const auth = getStoredAuth();
  const response = await fetch(`${API_URL}/api/ai/executive-report/export/pdf`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(auth?.accessToken ? { Authorization: `Bearer ${auth.accessToken}` } : {}),
    },
    body: JSON.stringify(report),
  });

  await downloadBlobResponse(response, "ai-executive-report.pdf");
}

export async function downloadExecutiveReportPdfById(reportId: number) {
  const auth = getStoredAuth();
  const response = await fetch(
    `${API_URL}/api/ai/executive-report/${reportId}/export/pdf`,
    {
      headers: auth?.accessToken
        ? { Authorization: `Bearer ${auth.accessToken}` }
        : {},
    },
  );

  await downloadBlobResponse(response, `ai-executive-report-${reportId}.pdf`);
}
