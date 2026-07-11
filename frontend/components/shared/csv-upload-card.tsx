"use client";

import { useRef, useState } from "react";
import { UploadCloud } from "lucide-react";
import { SectionCard } from "@/components/shared/section-card";
import { Button } from "@/components/ui/button";
import { uploadIngestionCsv } from "@/lib/api";
import type { IngestionRun } from "@/types";

export function CsvUploadCard({ onUploaded }: { onUploaded: () => void }) {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastResult, setLastResult] = useState<IngestionRun | null>(null);

  async function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file) return;
    setError(null);
    setLastResult(null);
    setIsUploading(true);
    try {
      const result = await uploadIngestionCsv(file);
      setLastResult(result);
      onUploaded();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to upload CSV file");
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  }

  return (
    <SectionCard
      title="Upload Transaction CSV"
      description="Runs the ingestion pipeline (raw -> staging -> clean/rejected) and produces a pipeline run below"
    >
      <div className="flex flex-wrap items-center gap-3">
        <input ref={fileInputRef} type="file" accept=".csv" className="hidden" onChange={handleFileChange} />
        <Button onClick={() => fileInputRef.current?.click()} disabled={isUploading}>
          <UploadCloud className="mr-2 h-4 w-4" />
          {isUploading ? "Uploading..." : "Upload CSV"}
        </Button>
        {lastResult && (
          <p className="text-xs text-muted-foreground">
            Run {lastResult.runId}: {lastResult.acceptedRows} accepted, {lastResult.rejectedRows} rejected,{" "}
            {lastResult.deadLetterRows} dead-lettered out of {lastResult.totalRows} rows.
          </p>
        )}
        {error && <p className="text-xs text-destructive">{error}</p>}
      </div>
    </SectionCard>
  );
}
