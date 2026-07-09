import { ShieldOff } from "lucide-react";

export function AccessDenied({
  message = "You don't have permission to view this page.",
}: {
  message?: string;
}) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 rounded-lg border border-dashed border-border py-24 text-center">
      <ShieldOff className="h-8 w-8 text-destructive" />
      <p className="text-base font-semibold text-foreground">Access Denied</p>
      <p className="max-w-sm text-sm text-muted-foreground">{message}</p>
    </div>
  );
}
