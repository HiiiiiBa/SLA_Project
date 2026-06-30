import { cn } from "@/lib/utils";

export function Select({
  className,
  children,
  ...props
}: React.SelectHTMLAttributes<HTMLSelectElement>) {
  return (
    <select
      className={cn(
        "w-full rounded-lg border border-border bg-card px-4 py-2.5 text-sm text-foreground shadow-sm outline-none transition focus:border-primary focus:ring-4 focus:ring-primary/20 focus:shadow-md cursor-pointer",
        className,
      )}
      {...props}
    >
      {children}
    </select>
  );
}
