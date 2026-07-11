import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { TransactionStatusBadge } from "@/components/badges/transaction-status-badge";
import { formatCurrency, formatDate } from "@/lib/formatters";
import type { BankingTransaction, RiskScore } from "@/types";

function DetailRow({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex items-center justify-between gap-4 py-1">
      <span className="text-xs text-muted-foreground">{label}</span>
      <span className="text-right text-xs font-medium text-foreground">{value}</span>
    </div>
  );
}

export function LinkedTransactionCard({
  transaction,
  riskScore,
}: {
  transaction: BankingTransaction | null;
  riskScore?: RiskScore | null;
}) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Linked Transaction</CardTitle>
      </CardHeader>
      <CardContent>
        {!transaction ? (
          <p className="text-sm text-muted-foreground">Transaction details unavailable.</p>
        ) : (
          <div className="space-y-3 text-sm">
            <div className="flex items-center justify-between">
              <Link
                href={`/transactions?search=${transaction.transactionId}`}
                className="font-medium text-primary hover:underline"
              >
                {transaction.transactionId}
              </Link>
              <TransactionStatusBadge status={transaction.status} />
            </div>
            <p className="text-lg font-semibold">{formatCurrency(transaction.amount, transaction.currency)}</p>
            <p className="text-muted-foreground">
              {transaction.transactionType} via {transaction.channel} - {transaction.country}
            </p>

            <div className="divide-y divide-border rounded-md border border-border px-3">
              <DetailRow label="Customer" value={transaction.customerId} />
              <DetailRow label="Source Account" value={transaction.sourceAccountId} />
              <DetailRow label="Destination Account" value={transaction.destinationAccountId ?? "-"} />
              <DetailRow label="Merchant Category" value={transaction.merchantCategory ?? "-"} />
              <DetailRow label="Device ID" value={transaction.deviceId ?? "-"} />
              <DetailRow label="IP Address" value={transaction.ipAddress ?? "-"} />
              <DetailRow label="Created" value={formatDate(transaction.createdAt)} />
            </div>

            {riskScore && (
              <div className="space-y-1.5 rounded-md border border-border bg-muted/40 px-3 py-2.5">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    Why this was flagged
                  </span>
                  <Badge variant="info">Score {riskScore.finalScore}</Badge>
                </div>
                {riskScore.triggeredRules && (
                  <p className="text-xs text-muted-foreground">Rules: {riskScore.triggeredRules}</p>
                )}
                {riskScore.explanation && <p className="text-xs text-foreground">{riskScore.explanation}</p>}
                {riskScore.mlExplanation && (
                  <p className="text-xs text-muted-foreground">ML: {riskScore.mlExplanation}</p>
                )}
              </div>
            )}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
