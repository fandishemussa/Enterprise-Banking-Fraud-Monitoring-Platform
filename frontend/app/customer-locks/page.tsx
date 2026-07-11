"use client";

import { useState } from "react";
import { Check, X } from "lucide-react";
import { PageHeader } from "@/components/layout/page-header";
import { DemoDataBanner } from "@/components/shared/demo-data-banner";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { DataTable } from "@/components/tables/data-table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogClose, DialogFooter } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { useAuth } from "@/hooks/use-auth";
import { useApiResource } from "@/hooks/use-api-resource";
import { getPendingLockRequests, approveLockRequest, rejectLockRequest } from "@/lib/api";
import { formatDate } from "@/lib/formatters";
import type { ColumnDef } from "@tanstack/react-table";
import type { CustomerLockRequest } from "@/types";

export default function CustomerLocksPage() {
  const { hasRole } = useAuth();
  const isAdmin = hasRole("ADMIN");
  const { data: requests, isLoading, error, usingMockData, refetch } = useApiResource(getPendingLockRequests);

  const [reviewTarget, setReviewTarget] = useState<{ request: CustomerLockRequest; action: "approve" | "reject" } | null>(
    null,
  );
  const [notes, setNotes] = useState("");
  const [actionError, setActionError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleReview() {
    if (!reviewTarget) return;
    setActionError(null);
    setIsSubmitting(true);
    try {
      if (reviewTarget.action === "approve") {
        await approveLockRequest(reviewTarget.request.lockRequestId, notes || undefined);
      } else {
        await rejectLockRequest(reviewTarget.request.lockRequestId, notes || undefined);
      }
      setReviewTarget(null);
      setNotes("");
      refetch();
    } catch (err) {
      setActionError(err instanceof Error ? err.message : "Failed to review lock request");
    } finally {
      setIsSubmitting(false);
    }
  }

  const columns: ColumnDef<CustomerLockRequest>[] = [
    { accessorKey: "lockRequestId", header: "Request ID" },
    { accessorKey: "customerId", header: "Customer" },
    { accessorKey: "requestedBy", header: "Requested By" },
    { accessorKey: "requestedByRole", header: "Role" },
    { accessorKey: "reason", header: "Reason", cell: ({ getValue }) => (getValue() as string) ?? "-" },
    { accessorKey: "createdAt", header: "Requested", cell: ({ getValue }) => formatDate(getValue() as string) },
    ...(isAdmin
      ? [
          {
            id: "actions",
            header: "Actions",
            cell: ({ row }: { row: { original: CustomerLockRequest } }) => (
              <div className="flex items-center gap-1">
                <Button
                  variant="ghost"
                  size="icon"
                  aria-label="Approve lock request"
                  onClick={() => setReviewTarget({ request: row.original, action: "approve" })}
                >
                  <Check className="h-3.5 w-3.5 text-emerald-600" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon"
                  aria-label="Reject lock request"
                  onClick={() => setReviewTarget({ request: row.original, action: "reject" })}
                >
                  <X className="h-3.5 w-3.5 text-destructive" />
                </Button>
              </div>
            ),
          } satisfies ColumnDef<CustomerLockRequest>,
        ]
      : []),
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Customer Locks"
        description={
          isAdmin
            ? "Pending fraud-lock requests awaiting approval"
            : "Pending fraud-lock requests you've submitted or that are awaiting admin approval"
        }
      />
      {usingMockData && <DemoDataBanner />}
      {actionError && <p className="text-sm text-destructive">{actionError}</p>}

      {isLoading ? (
        <LoadingState label="Loading lock requests..." />
      ) : error ? (
        <ErrorState message={error} onRetry={refetch} />
      ) : (
        <DataTable
          columns={columns}
          data={requests ?? []}
          searchPlaceholder="Search lock requests..."
          emptyMessage="No pending lock requests."
        />
      )}

      <Dialog open={reviewTarget !== null} onOpenChange={(open) => !open && setReviewTarget(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {reviewTarget?.action === "approve" ? "Approve" : "Reject"} Lock Request{" "}
              {reviewTarget?.request.lockRequestId}
            </DialogTitle>
            <DialogClose onClick={() => setReviewTarget(null)} />
          </DialogHeader>
          <div className="space-y-3">
            <p className="text-sm text-muted-foreground">
              Customer {reviewTarget?.request.customerId}, requested by {reviewTarget?.request.requestedBy}.
            </p>
            {reviewTarget?.request.reason && (
              <Badge variant="neutral">{reviewTarget.request.reason}</Badge>
            )}
            <Input placeholder="Notes (optional)" value={notes} onChange={(e) => setNotes(e.target.value)} />
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setReviewTarget(null)}>
                Cancel
              </Button>
              <Button
                onClick={handleReview}
                variant={reviewTarget?.action === "reject" ? "destructive" : "default"}
                disabled={isSubmitting}
              >
                {isSubmitting ? "Saving..." : reviewTarget?.action === "approve" ? "Approve" : "Reject"}
              </Button>
            </DialogFooter>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
