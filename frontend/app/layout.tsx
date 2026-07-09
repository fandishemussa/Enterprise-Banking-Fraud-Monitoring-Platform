import type { Metadata } from "next";
import { AppShell } from "@/components/layout/app-shell";
import { AuthProvider } from "@/hooks/use-auth";
import "./globals.css";

export const metadata: Metadata = {
  title: "Enterprise Banking Fraud Monitoring Platform",
  description: "Transaction monitoring, fraud alerts, and investigation workflows dashboard",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className="antialiased">
        <AuthProvider>
          <AppShell>{children}</AppShell>
        </AuthProvider>
      </body>
    </html>
  );
}
