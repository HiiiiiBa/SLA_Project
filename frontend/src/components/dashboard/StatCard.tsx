import { cn } from "@/lib/utils";

export function StatCard({
  title,
  value,
  hint,
  icon,
  accent = "emerald",
}: {
  title: string;
  value: string | number;
  hint?: string;
  icon: React.ReactNode;
  accent?: "emerald" | "blue" | "amber" | "red";
}) {
  const accents = {
    emerald: "from-emerald-500/10 to-emerald-500/5 text-emerald-600",
    blue: "from-blue-500/10 to-blue-500/5 text-blue-600",
    amber: "from-amber-500/10 to-amber-500/5 text-amber-600",
    red: "from-red-500/10 to-red-500/5 text-red-600",
  };

  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm font-medium text-muted">{title}</p>
          <p className="mt-3 text-3xl font-semibold tracking-tight text-heading">
            {value}
          </p>
          {hint && <p className="mt-2 text-sm text-slate-400">{hint}</p>}
        </div>
        <div
          className={cn(
            "flex h-12 w-12 items-center justify-center rounded-2xl bg-gradient-to-br",
            accents[accent],
          )}
        >
          {icon}
        </div>
      </div>
    </div>
  );
}
