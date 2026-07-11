"use client";

import { useCallback, useEffect, useState } from "react";
import {
  addCaseNote,
  deleteCaseNote,
  getCaseById,
  getCaseNotes,
  getCaseStatusHistory,
  getCaseTimeline,
  updateCase,
  updateCaseDecision,
  updateCaseNote,
} from "@/lib/api";
import type { CaseNote, CaseNotePayload, CaseStatusHistory, CaseTimelineEvent, InvestigationCase } from "@/types";

export function useCaseDetail(caseId: string) {
  const [investigationCase, setInvestigationCase] = useState<InvestigationCase | null>(null);
  const [timeline, setTimeline] = useState<CaseTimelineEvent[]>([]);
  const [notes, setNotes] = useState<CaseNote[]>([]);
  const [statusHistory, setStatusHistory] = useState<CaseStatusHistory[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [usingMockData, setUsingMockData] = useState(false);
  const [mutating, setMutating] = useState(false);

  const load = useCallback(() => {
    setIsLoading(true);
    setError(null);
    Promise.all([
      getCaseById(caseId),
      getCaseTimeline(caseId),
      getCaseNotes(caseId),
      getCaseStatusHistory(caseId),
    ])
      .then(([caseResult, timelineResult, notesResult, historyResult]) => {
        setInvestigationCase(caseResult.data);
        setTimeline(timelineResult.data);
        setNotes(notesResult.data);
        setStatusHistory(historyResult.data);
        setUsingMockData(caseResult.usingMockData || timelineResult.usingMockData || notesResult.usingMockData);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load case"))
      .finally(() => setIsLoading(false));
  }, [caseId]);

  useEffect(() => load(), [load]);

  async function updateStatus(status: string) {
    setMutating(true);
    try {
      await updateCase(caseId, { status });
      load();
    } finally {
      setMutating(false);
    }
  }

  async function updateAssignedTo(assignedTo: string) {
    setMutating(true);
    try {
      await updateCase(caseId, { assignedTo });
      load();
    } finally {
      setMutating(false);
    }
  }

  async function updateDecision(decision: string) {
    setMutating(true);
    try {
      await updateCaseDecision(caseId, decision);
      load();
    } finally {
      setMutating(false);
    }
  }

  async function addNote(payload: CaseNotePayload) {
    setMutating(true);
    try {
      await addCaseNote(caseId, payload);
      load();
    } finally {
      setMutating(false);
    }
  }

  async function editNote(noteId: string, payload: CaseNotePayload) {
    setMutating(true);
    try {
      await updateCaseNote(caseId, noteId, payload);
      load();
    } finally {
      setMutating(false);
    }
  }

  async function removeNote(noteId: string) {
    setMutating(true);
    try {
      await deleteCaseNote(caseId, noteId);
      load();
    } finally {
      setMutating(false);
    }
  }

  return {
    investigationCase,
    timeline,
    notes,
    statusHistory,
    isLoading,
    error,
    usingMockData,
    mutating,
    refetch: load,
    updateStatus,
    updateAssignedTo,
    updateDecision,
    addNote,
    editNote,
    removeNote,
  };
}
