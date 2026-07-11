"use client";

import Link from "next/link";
import { Network } from "lucide-react";
import { SectionCard } from "@/components/shared/section-card";
import { Badge } from "@/components/ui/badge";
import { LoadingState } from "@/components/shared/loading-state";
import { useLinkedCustomers } from "@/hooks/use-linked-customers";

export function LinkedCustomersCard({ customerId }: { customerId: string }) {
  const { data: linked, isLoading } = useLinkedCustomers(customerId);

  return (
    <SectionCard
      title="Linked Customers"
      description="Other customers who share a device ID or IP address with this one - a fraud ring signal"
    >
      {isLoading ? (
        <LoadingState label="Checking for linked customers..." />
      ) : !linked || linked.length === 0 ? (
        <p className="flex items-center gap-2 text-sm text-muted-foreground">
          <Network className="h-4 w-4" />
          No linked customers found.
        </p>
      ) : (
        <ul className="divide-y divide-border">
          {linked.map((entry) => (
            <li key={entry.customerId} className="flex flex-col gap-1.5 py-3">
              <div className="flex items-center justify-between gap-2">
                <Link href={`/customers/${entry.customerId}`} className="text-sm font-medium text-primary hover:underline">
                  {entry.fullName} ({entry.customerId})
                </Link>
                <Badge variant="destructive">
                  {entry.sharedDeviceIds.length + entry.sharedIpAddresses.length} shared identifier
                  {entry.sharedDeviceIds.length + entry.sharedIpAddresses.length === 1 ? "" : "s"}
                </Badge>
              </div>
              {entry.sharedDeviceIds.length > 0 && (
                <p className="text-xs text-muted-foreground">Shared devices: {entry.sharedDeviceIds.join(", ")}</p>
              )}
              {entry.sharedIpAddresses.length > 0 && (
                <p className="text-xs text-muted-foreground">Shared IPs: {entry.sharedIpAddresses.join(", ")}</p>
              )}
            </li>
          ))}
        </ul>
      )}
    </SectionCard>
  );
}
