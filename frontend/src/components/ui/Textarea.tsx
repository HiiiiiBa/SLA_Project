import { cn } from "@/lib/utils";

export function Textarea({
  className,
  ...props
}: React.TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return (
    <textarea
      className={cn(
        "w-full rounded-lg border border-border bg-card px-4 py-2.5 text-sm text-foreground shadow-sm outline-none transition placeholder:text-muted focus:border-primary focus:ring-4 focus:ring-primary/20 focus:shadow-md resize-none",
        className,
      )}
      {...props}
    />
  );
}
