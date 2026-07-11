"use client";

import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import type { PublishStreamingTransactionPayload, TransactionChannel, TransactionType } from "@/types";

const TRANSACTION_TYPES: TransactionType[] = ["TRANSFER", "WITHDRAWAL", "DEPOSIT", "PAYMENT", "CARD_PAYMENT"];
const CHANNELS: TransactionChannel[] = ["MOBILE", "WEB", "ATM", "POS", "BRANCH"];

interface PublishStreamingTransactionFormProps {
  onPublish: (payload: PublishStreamingTransactionPayload) => Promise<string>;
}

export function PublishStreamingTransactionForm({ onPublish }: PublishStreamingTransactionFormProps) {
  const [sourceAccountId, setSourceAccountId] = useState("");
  const [amount, setAmount] = useState("");
  const [currency, setCurrency] = useState("USD");
  const [transactionType, setTransactionType] = useState<TransactionType>("PAYMENT");
  const [channel, setChannel] = useState<TransactionChannel>("WEB");
  const [country, setCountry] = useState("");
  const [merchantCategory, setMerchantCategory] = useState("");
  const [deviceId, setDeviceId] = useState("");
  const [result, setResult] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setResult(null);
    setIsSubmitting(true);
    try {
      const eventId = await onPublish({
        sourceAccountId,
        amount: Number(amount),
        currency,
        transactionType,
        channel,
        country,
        merchantCategory: merchantCategory || undefined,
        deviceId: deviceId || undefined,
      });
      setResult(eventId);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to publish event");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Publish Streaming Transaction</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="grid gap-3 sm:grid-cols-2">
          <Input placeholder="Source account ID" value={sourceAccountId} onChange={(e) => setSourceAccountId(e.target.value)} required />
          <Input type="number" step="0.01" placeholder="Amount" value={amount} onChange={(e) => setAmount(e.target.value)} required />
          <Input placeholder="Currency" value={currency} onChange={(e) => setCurrency(e.target.value)} required />
          <Input placeholder="Country" value={country} onChange={(e) => setCountry(e.target.value)} required />
          <Select value={transactionType} onChange={(e) => setTransactionType(e.target.value as TransactionType)}>
            {TRANSACTION_TYPES.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </Select>
          <Select value={channel} onChange={(e) => setChannel(e.target.value as TransactionChannel)}>
            {CHANNELS.map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </Select>
          <Input placeholder="Merchant category (optional)" value={merchantCategory} onChange={(e) => setMerchantCategory(e.target.value)} />
          <Input placeholder="Device ID (optional)" value={deviceId} onChange={(e) => setDeviceId(e.target.value)} />
          <div className="sm:col-span-2">
            {error && <p className="mb-2 text-sm text-destructive">{error}</p>}
            {result && <p className="mb-2 text-sm text-emerald-600">Published as event {result}</p>}
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Publishing..." : "Publish to raw-transactions"}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}
