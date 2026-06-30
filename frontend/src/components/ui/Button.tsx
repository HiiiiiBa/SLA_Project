import { cn } from "@/lib/utils";

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "secondary" | "ghost" | "danger";
  loading?: boolean;
}

export function Button({
  className,
  variant = "primary",
  loading,
  children,
  disabled,
  ...props
}: ButtonProps) {
  return (
    <button
      className={cn(
        "inline-flex items-center justify-center gap-2 rounded-lg px-4 py-2.5 text-sm font-semibold transition-all duration-200 disabled:cursor-not-allowed disabled:opacity-60",
        variant === "primary" &&
          "bg-gradient-to-r from-primary to-accent text-white shadow-lg shadow-primary/30 hover:shadow-xl hover:shadow-primary/40 hover:scale-105 active:scale-95",
        variant === "secondary" &&
          "border border-border bg-card text-foreground hover:bg-card/80 hover:border-primary/50 shadow-md hover:shadow-lg",
        variant === "ghost" &&
          "text-foreground hover:bg-card/50 transition-colors",
        variant === "danger" &&
          "border border-error/50 bg-error/10 text-error hover:bg-error/20 shadow-lg shadow-error/10",
        className,
      )}
      disabled={disabled || loading}
      {...props}
    >
      {loading && (
        <span className="h-4 w-4 animate-spin rounded-full border-2 border-foreground/30 border-t-foreground" />
      )}
      {children}
    </button>
  );
}
