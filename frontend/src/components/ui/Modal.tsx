"use client";

import { useEffect } from "react";
import { X } from "lucide-react";
import { cn } from "@/lib/utils";

interface ModalProps {
  open: boolean;
  onClose: () => void;
  title: string;
  description?: string;
  children: React.ReactNode;
  className?: string;
}

export function Modal({
  open,
  onClose,
  title,
  description,
  children,
  className,
}: ModalProps) {
  useEffect(() => {
    if (!open) return;
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") onClose();
    };
    document.body.style.overflow = "hidden";
    window.addEventListener("keydown", onKeyDown);
    return () => {
      document.body.style.overflow = "";
      window.removeEventListener("keydown", onKeyDown);
    };
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <button
        className="absolute inset-0 bg-black/50 backdrop-blur-md"
        onClick={onClose}
        aria-label="Fermer"
      />
      <div
        className={cn(
          "relative z-10 w-full max-w-lg rounded-xl border border-border bg-card p-6 shadow-2xl shadow-black/40",
          className,
        )}
      >
        <div className="mb-6 flex items-start justify-between gap-4">
          <div>
            <h2 className="text-xl font-bold text-foreground">
              {title}
            </h2>
            {description && (
              <p className="mt-1 text-sm text-muted">
                {description}
              </p>
            )}
          </div>
          <button
            onClick={onClose}
            className="rounded-lg border border-border p-2 text-muted hover:text-foreground hover:bg-card/50 transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        {children}
      </div>
    </div>
  );
}
