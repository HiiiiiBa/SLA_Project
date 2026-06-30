import { cn } from "@/lib/utils";
import type { LucideIcon } from "lucide-react";

export function EmptyState({
  icon: Icon,
  title,
  description,
  className,
}: {
  icon: LucideIcon;
  title: string;
  description?: string;
  className?: string;
}) {
  return (
    <div
      className={cn(
        "flex flex-col items-center gap-3 px-6 py-14 text-center",
        className,
      )}
    >
      <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-primary/15 to-accent/10 ring-1 ring-primary/20">
        <Icon className="h-7 w-7 text-primary" />
      </div>
      <p className="text-sm font-semibold text-heading">{title}</p>
      {description && <p className="max-w-md text-sm text-muted">{description}</p>}
    </div>
  );
}
