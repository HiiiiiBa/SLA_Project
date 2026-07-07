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
  /** Max width utility, e.g. max-w-2xl */
  size?: "default" | "large";
}

export function Modal({
  open,
  onClose,
  title,
  description,
  children,
  className,
  size = "default",
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
    <div className="fixed inset-0 z-50 overflow-y-auto p-4 sm:p-6">
      <button
        className="fixed inset-0 bg-black/50 backdrop-blur-md"
        onClick={onClose}
        aria-label="Fermer"
      />
      <div className="flex min-h-full items-start justify-center sm:items-center">
        <div
          className={cn(
            "relative z-10 flex w-full flex-col rounded-xl border border-border bg-card shadow-2xl shadow-black/40",
            "max-h-[calc(100vh-2rem)] sm:max-h-[min(90vh,52rem)]",
            size === "large" ? "max-w-2xl" : "max-w-lg",
            className,
          )}
        >
          <div className="flex shrink-0 items-start justify-between gap-4 border-b border-border/60 px-6 py-4">
            <div className="min-w-0 pr-2">
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
              className="shrink-0 rounded-lg border border-border p-2 text-muted transition-colors hover:bg-card/50 hover:text-foreground"
            >
              <X className="h-5 w-5" />
            </button>
          </div>
          <div className="scroll-area min-h-0 flex-1 overflow-y-auto overscroll-contain bg-card px-6 py-4">
            {children}
          </div>
        </div>
      </div>
    </div>
  );
}
