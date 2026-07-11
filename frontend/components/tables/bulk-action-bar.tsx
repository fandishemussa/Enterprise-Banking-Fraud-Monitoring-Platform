import { X } from "lucide-react";
import { Button } from "@/components/ui/button";

export function BulkActionBar({
  count,
  onClear,
  children,
}: {
  count: number;
  onClear: () => void;
  children: React.ReactNode;
}) {
  return (
    <div className="flex items-center gap-2 rounded-md border border-border bg-muted/50 px-3 py-1.5">
      <span className="text-xs font-medium text-foreground">{count} selected</span>
      <div className="flex items-center gap-1.5">{children}</div>
      <Button variant="ghost" size="icon" aria-label="Clear selection" onClick={onClear} className="h-7 w-7">
        <X className="h-3.5 w-3.5" />
      </Button>
    </div>
  );
}
