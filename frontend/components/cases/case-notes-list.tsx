"use client";

import { useState } from "react";
import { Pencil, Trash2, X, Check } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { formatDate } from "@/lib/formatters";
import type { CaseNote } from "@/types";

interface CaseNotesListProps {
  notes: CaseNote[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (noteId: string, noteText: string) => Promise<void>;
  onDelete: (noteId: string) => Promise<void>;
}

export function CaseNotesList({ notes, canEdit, canDelete, onEdit, onDelete }: CaseNotesListProps) {
  const [editingId, setEditingId] = useState<string | null>(null);
  const [draft, setDraft] = useState("");

  if (notes.length === 0) {
    return <p className="text-sm text-muted-foreground">No notes yet.</p>;
  }

  return (
    <div className="space-y-3">
      {notes.map((note) => (
        <div key={note.noteId} className="rounded-md border border-border p-3 text-sm">
          <div className="flex items-center justify-between">
            <span className="font-medium">
              {note.authorUsername} <span className="text-xs text-muted-foreground">({note.authorRole})</span>
            </span>
            <div className="flex items-center gap-2">
              {note.internalOnly && <Badge variant="neutral">Internal</Badge>}
              <span className="text-xs text-muted-foreground">{formatDate(note.createdAt)}</span>
              {canEdit && editingId !== note.noteId && (
                <Button
                  variant="ghost"
                  size="icon"
                  aria-label="Edit note"
                  onClick={() => {
                    setEditingId(note.noteId);
                    setDraft(note.noteText);
                  }}
                >
                  <Pencil className="h-3.5 w-3.5" />
                </Button>
              )}
              {canDelete && (
                <Button variant="ghost" size="icon" aria-label="Delete note" onClick={() => onDelete(note.noteId)}>
                  <Trash2 className="h-3.5 w-3.5 text-destructive" />
                </Button>
              )}
            </div>
          </div>
          {editingId === note.noteId ? (
            <div className="mt-2 space-y-2">
              <textarea
                className="w-full rounded-md border border-border bg-background p-2 text-sm"
                rows={3}
                value={draft}
                onChange={(e) => setDraft(e.target.value)}
              />
              <div className="flex justify-end gap-2">
                <Button variant="ghost" size="icon" aria-label="Cancel" onClick={() => setEditingId(null)}>
                  <X className="h-3.5 w-3.5" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon"
                  aria-label="Save"
                  onClick={async () => {
                    await onEdit(note.noteId, draft);
                    setEditingId(null);
                  }}
                >
                  <Check className="h-3.5 w-3.5 text-emerald-600" />
                </Button>
              </div>
            </div>
          ) : (
            <p className="mt-1 text-muted-foreground">{note.noteText}</p>
          )}
        </div>
      ))}
    </div>
  );
}
