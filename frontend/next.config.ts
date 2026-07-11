import type { NextConfig } from "next";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";
const ML_SERVICE_URL = process.env.NEXT_PUBLIC_ML_SERVICE_URL ?? "http://localhost:8000";

// The dashboard fetches directly from these origins (see lib/endpoints.ts), so connect-src must
// allow them explicitly - CSP's default-src 'self' would otherwise block every API call.
const connectSrc = ["'self'", API_BASE_URL, ML_SERVICE_URL].join(" ");

// Next.js dev mode (webpack/Turbopack HMR, React DevTools stack reconstruction) calls eval() -
// never happens in a production build, so 'unsafe-eval' is scoped to dev only.
const scriptSrc = process.env.NODE_ENV === "production"
  ? "script-src 'self' 'unsafe-inline'"
  : "script-src 'self' 'unsafe-inline' 'unsafe-eval'";

const CONTENT_SECURITY_POLICY = [
  "default-src 'self'",
  scriptSrc,
  "style-src 'self' 'unsafe-inline'",
  "img-src 'self' data:",
  "font-src 'self' data:",
  `connect-src ${connectSrc}`,
  "frame-ancestors 'none'",
  "base-uri 'self'",
  "form-action 'self'",
].join("; ");

const nextConfig: NextConfig = {
  reactStrictMode: true,
  output: "standalone",
  async headers() {
    return [
      {
        source: "/:path*",
        headers: [
          { key: "Content-Security-Policy", value: CONTENT_SECURITY_POLICY },
          { key: "X-Content-Type-Options", value: "nosniff" },
          { key: "X-Frame-Options", value: "DENY" },
          { key: "Referrer-Policy", value: "strict-origin-when-cross-origin" },
          { key: "Permissions-Policy", value: "camera=(), microphone=(), geolocation=()" },
          // Only meaningful once served over HTTPS (a plain-HTTP response ignores this header) -
          // harmless to send in local dev, real in prod behind TLS termination.
          { key: "Strict-Transport-Security", value: "max-age=63072000; includeSubDomains" },
        ],
      },
    ];
  },
};

export default nextConfig;
