import { cn } from "@/lib/utils";

export function Input({
  className,
  ...props
}: React.InputHTMLAttributes<HTMLInputElement>) {
  return (
    <input
      className={cn(
        "w-full rounded-lg border border-border bg-card px-4 py-2.5 text-sm text-foreground shadow-sm outline-none transition placeholder:text-muted focus:border-primary focus:ring-4 focus:ring-primary/20 focus:shadow-md",
        className,
      )}
      {...props}
    />
  );
}
