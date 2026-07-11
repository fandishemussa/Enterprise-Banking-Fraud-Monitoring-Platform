"use client";

import { Download } from "lucide-react";
import { Button } from "@/components/ui/button";
import { exportToCsv } from "@/lib/csv-export";

export function ExportCsvButton<T extends object>({
  filename,
  rows,
}: {
  filename: string;
  rows: T[];
}) {
  return (
    <Button variant="outline" size="sm" disabled={rows.length === 0} onClick={() => exportToCsv(filename, rows)}>
      <Download className="mr-1.5 h-3.5 w-3.5" />
      Export CSV
    </Button>
  );
}
