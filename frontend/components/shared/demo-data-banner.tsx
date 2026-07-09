import { Info } from "lucide-react";

export function DemoDataBanner() {
  return (
    <div className="flex items-center gap-2 rounded-md border border-amber-300 bg-amber-50 px-3 py-2 text-sm text-amber-800 dark:border-amber-500/30 dark:bg-amber-500/10 dark:text-amber-400">
      <Info className="h-4 w-4 shrink-0" />
      <span>Using demo data because backend endpoint is unavailable.</span>
    </div>
  );
}
