interface HeaderProps {
  title: string;
  description?: string;
  action?: React.ReactNode;
}

export function Header({ title, description, action }: HeaderProps) {
  return (
    <div className="mb-8 flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
      <div>
        <h1 className="text-4xl font-bold tracking-tight gradient-text">{title}</h1>
        {description && (
          <p className="mt-3 max-w-2xl text-base leading-relaxed text-muted">{description}</p>
        )}
      </div>
      {action && <div className="flex gap-3">{action}</div>}
    </div>
  );
}
