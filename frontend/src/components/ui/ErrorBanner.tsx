"use client";

import { AlertTriangle } from "lucide-react";

export function ErrorBanner({
  message,
  onRetry,
}: {
  message: string;
  onRetry?: () => void;
}) {
  return (
    <div className="mb-6 flex items-start gap-3 rounded-2xl border border-red-200 bg-red-50 px-5 py-4 text-red-800">
      <AlertTriangle className="mt-0.5 h-5 w-5 shrink-0" />
      <div className="flex-1">
        <p className="font-medium">Impossible de charger les données</p>
        <p className="mt-1 text-sm text-red-700">{message}</p>
        <p className="mt-2 text-xs text-red-600">
          Vérifiez que le backend tourne sur le port 8080, puis redémarrez-le si
          nécessaire : <code className="rounded bg-red-100 px-1">mvn spring-boot:run</code>
        </p>
      </div>
      {onRetry && (
        <button
          onClick={onRetry}
          className="rounded-xl border border-red-300 bg-white px-3 py-1.5 text-sm font-medium text-red-700 hover:bg-red-100"
        >
          Réessayer
        </button>
      )}
    </div>
  );
}
