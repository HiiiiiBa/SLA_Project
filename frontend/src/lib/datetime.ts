export function toApiDateTime(value: string): string {
  if (!value) return value;
  return value.length === 16 ? `${value}:00` : value;
}

export function toInputDateTime(value?: string | null): string {
  if (!value) return "";
  return value.slice(0, 16);
}

export function nowForInput(): string {
  const date = new Date();
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
  return date.toISOString().slice(0, 16);
}
