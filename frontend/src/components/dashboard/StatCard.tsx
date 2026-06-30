import { cn } from "@/lib/utils";

export function StatCard({
  title,
  value,
  hint,
  icon,
  accent = "blue",
}: {
  title: string;
  value: string | number;
  hint?: string;
  icon: React.ReactNode;
  accent?: "emerald" | "blue" | "amber" | "red" | "cyan";
}) {
  const accents = {
    emerald: "from-success/20 to-success/5 text-success",
    blue: "from-primary/20 to-primary/5 text-primary",
    cyan: "from-accent/20 to-accent/5 text-accent",
    amber: "from-warning/20 to-warning/5 text-warning",
    red: "from-error/20 to-error/5 text-error",
  };

  return (
    <div className="surface-card surface-card-interactive p-6 hover:scale-[1.02] transform transition-all duration-300">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-muted">{title}</p>
          <p className="mt-3 text-4xl font-bold tracking-tight text-foreground">
            {value}
          </p>
          {hint && <p className="mt-2 text-xs text-muted">{hint}</p>}
        </div>
        <div
          className={cn(
            "flex h-14 w-14 items-center justify-center rounded-lg bg-gradient-to-br shadow-lg",
            accents[accent],
          )}
        >
          {icon}
        </div>
      </div>
    </div>
  );
}
