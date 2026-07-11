"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import type { CaseNotePayload } from "@/types";

interface CaseNoteFormProps {
  onSubmit: (payload: CaseNotePayload) => Promise<void>;
  disabled?: boolean;
}

export function CaseNoteForm({ onSubmit, disabled }: CaseNoteFormProps) {
  const [noteText, setNoteText] = useState("");
  const [internalOnly, setInternalOnly] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!noteText.trim()) return;
    setIsSubmitting(true);
    try {
      await onSubmit({ noteText, internalOnly });
      setNoteText("");
      setInternalOnly(false);
    } finally {
      setIsSubmitting(false);
    }
  }

  if (disabled) {
    return <p className="text-sm text-muted-foreground">You don&apos;t have permission to add notes to this case.</p>;
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-2">
      <textarea
        className="w-full rounded-md border border-border bg-background p-2 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/40"
        rows={3}
        placeholder="Add an analyst note..."
        value={noteText}
        onChange={(e) => setNoteText(e.target.value)}
      />
      <div className="flex items-center justify-between">
        <label className="flex items-center gap-2 text-xs text-muted-foreground">
          <input type="checkbox" checked={internalOnly} onChange={(e) => setInternalOnly(e.target.checked)} />
          Internal only
        </label>
        <Button type="submit" size="sm" disabled={isSubmitting || !noteText.trim()}>
          {isSubmitting ? "Adding..." : "Add Note"}
        </Button>
      </div>
    </form>
  );
}
