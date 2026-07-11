import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { TransactionStatusBadge } from "@/components/badges/transaction-status-badge";
import { formatCurrency, formatDate } from "@/lib/formatters";
import type { BankingTransaction } from "@/types";

export function CustomerRecentTransactions({ transactions }: { transactions: BankingTransaction[] }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Recent Transactions</CardTitle>
      </CardHeader>
      <CardContent>
        {transactions.length === 0 ? (
          <p className="text-sm text-muted-foreground">No transactions recorded.</p>
        ) : (
          <div className="space-y-2">
            {transactions.map((t) => (
              <div key={t.transactionId} className="flex items-center justify-between rounded-md border border-border p-2 text-sm">
                <div>
                  <p className="font-medium">{t.transactionId}</p>
                  <p className="text-xs text-muted-foreground">{formatDate(t.createdAt)}</p>
                </div>
                <div className="flex items-center gap-2">
                  <span className="font-medium">{formatCurrency(t.amount, t.currency)}</span>
                  <TransactionStatusBadge status={t.status} />
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
