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
    <div className="mb-6 flex items-start gap-3 rounded-lg border border-error/50 bg-error/10 px-5 py-4 text-error animate-slide-in">
      <AlertTriangle className="mt-0.5 h-5 w-5 shrink-0 flex-shrink-0" />
      <div className="flex-1">
        <p className="font-semibold">Impossible de charger les données</p>
        <p className="mt-1 text-sm opacity-90">{message}</p>
        <p className="mt-2 text-xs opacity-80">
          Vérifiez que le backend tourne sur le port 8080, puis redémarrez-le si
          nécessaire : <code className="rounded bg-error/20 px-1 py-0.5 font-mono">mvn spring-boot:run</code>
        </p>
      </div>
      {onRetry && (
        <button
          onClick={onRetry}
          className="rounded-lg border border-error/30 bg-error/10 px-3 py-1.5 text-sm font-semibold text-error hover:bg-error/20 transition-colors flex-shrink-0"
        >
          Réessayer
        </button>
      )}
    </div>
  );
}
