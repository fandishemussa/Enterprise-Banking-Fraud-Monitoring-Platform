# Fraud Monitoring Dashboard

Enterprise Next.js dashboard for the Banking Fraud Monitoring Platform — transaction monitoring,
fraud alerts, investigation cases, data engineering pipeline observability, and ML service health,
all in one place.

## Overview

The dashboard talks to the Spring Boot backend (`http://localhost:8080`) first for every resource.
If a backend call fails (service down, endpoint unreachable, timeout), the UI transparently falls
back to realistic mock data and shows a banner: **"Using demo data because backend endpoint is
unavailable."** This means the dashboard is fully explorable even with no backend running at all.

## Tech stack

- Next.js 16 (App Router) + TypeScript
- Tailwind CSS v4 (CSS-first theming via `@theme` in `app/globals.css`)
- Hand-rolled shadcn-style UI primitives (button, card, badge, table, tabs, select, dialog,
  dropdown-menu) built with `class-variance-authority` — no Radix dependency
- TanStack Table for sortable, searchable, paginated tables
- Recharts for the dashboard charts
- Lucide React icons

## Folder structure

```
frontend/
├── app/                  # Routes (App Router) - one folder per sidebar page
├── components/
│   ├── layout/            # Sidebar, topbar, app shell, page header
│   ├── dashboard/          # KPI cards, charts, recent alerts, pipeline health
│   ├── tables/              # Generic DataTable + one table per entity
│   ├── badges/               # Risk/alert/case/transaction/pipeline status badges
│   ├── shared/                 # Loading/error/empty states, demo data banner
│   └── ui/                       # Button, card, badge, input, table, tabs, select, dialog, dropdown
├── lib/                  # api.ts, endpoints.ts, mock-data.ts, formatters.ts, utils.ts
├── hooks/                # use-dashboard, use-transactions, use-alerts, use-cases,
│                         # use-customers, use-pipelines, use-ml-service
└── types/index.ts        # All shared TypeScript types (camelCase, mirrors backend DTOs)
```

## Environment variables

Copy `.env.local.example` to `.env.local`:

```
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_ML_SERVICE_URL=http://localhost:8000
```

## Installation

```bash
cd frontend
npm install
copy .env.local.example .env.local
```

## Run locally

```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) — it redirects to `/dashboard`.

## Build

```bash
npm run build
npm run start
```

## Connecting to the backend services

- **Spring Boot backend**: start it on port 8080 (see the repo root README/QUICKSTART). All list/detail
  endpoints under `/api/v1/*` are called via `lib/api.ts`, which unwraps the `ApiResponse<T>` envelope
  (`{ success, message, data, timestamp }`) that every Spring Boot endpoint returns.
- **FastAPI ML service**: start it on port 8000 (see `ml-service/README.md`). Its `/health` and
  `/api/v1/model-info` responses are consumed directly (no envelope) by the `/ml-service` page and
  the dashboard's ML summary card.
- Requests time out after 4 seconds and fall back to mock data rather than hanging the UI.

## Available pages

| Route | Purpose |
|---|---|
| `/dashboard` | KPIs, fraud trend/risk/alert/pipeline charts, recent alerts, pipeline & ML health |
| `/transactions` | Searchable, sortable transaction table |
| `/risk-scores` | Rule score, ML score, final score, risk level per transaction |
| `/alerts` | Fraud alerts with inline status change and analyst assignment |
| `/cases` | Investigation case list |
| `/customers` | Customer list with risk level and status |
| `/accounts` | Account list with balances |
| `/pipeline-observability` | Pipeline run KPIs, records processed/failed chart, run history |
| `/data-quality` | Quality score, check counts, per-check results table |
| `/dead-letter` | Dead-lettered rows with retry/ignore actions |
| `/audit-logs` | Full audit trail table |
| `/ml-service` | ML service health + loaded model metadata |
| `/admin` | Backend/ML URLs, connection status, risk rule summary |
| `/login` | Sign-in page (no sidebar/topbar chrome) |
| `/test-transactions` | Submit a test transaction with quick templates, see risk score + fraud alert result (ADMIN/ANALYST/TESTER) |
| `/users` | Create/edit/disable users and reset passwords (ADMIN only) |

## Authentication flow

Every page except `/login` requires a valid session. `hooks/use-auth.tsx` (`AuthProvider`/`useAuth`) hydrates the
session from `localStorage` on load, exposes `login`/`logout`/`hasRole`, and `components/layout/app-shell.tsx`
redirects to `/login` whenever there's no authenticated user. `lib/api.ts` attaches `Authorization: Bearer <token>`
to every request (see `getToken()` in `lib/auth.ts`); a `401` response clears the stored session and fires an
`auth:session-expired` window event that `AuthProvider` listens for to redirect back to `/login`. A `403` throws
`ApiAuthError` so pages can render `components/shared/access-denied.tsx` instead of an error state.

The sidebar (`components/layout/sidebar.tsx`) filters nav items by role (`item.roles` + `useAuth().hasRole()`), so
`Users` and `Test Transactions` are simply absent from the nav for roles that can't use them - the pages
themselves also render `AccessDenied` if visited directly, since hiding a link isn't authorization.

Default admin login: `admin` / `Admin@12345` (see the backend [QUICKSTART.md](../QUICKSTART.md) to change it).

## Mock data fallback behavior

Every `get*` function in `lib/api.ts` wraps its fetch in a try/catch: on any failure (network error,
non-2xx status, timeout) it returns the corresponding dataset from `lib/mock-data.ts` along with
`usingMockData: true`. Pages check that flag and render `<DemoDataBanner />` when it's true. Mutating
calls (update alert status, assign, create/update case, retry/ignore dead-letter) return `data: null`
on failure instead of throwing, so the UI can show a clear failure state without a full page crash.

## Manual testing checklist

- [ ] Backend running on `http://localhost:8080`
- [ ] ML service running on `http://localhost:8000`
- [ ] Frontend running on `http://localhost:3000`
- [ ] Visiting any page while logged out redirects to `/login`
- [ ] Logging in with `admin` / `Admin@12345` redirects to `/dashboard`
- [ ] `/` redirects to `/dashboard`
- [ ] Sidebar navigation covers all pages the current role can see, active link highlights correctly
- [ ] Dashboard KPIs, all 4 charts, recent alerts, and pipeline/ML summaries render
- [ ] Transaction/alert/case/customer/account/risk-score/audit-log tables load, sort, and search
- [ ] Alert status dropdown and "Assign" dialog work end-to-end against the real backend
- [ ] Dead Letter Queue retry/ignore buttons call the backend and refresh the table
- [ ] `/ml-service` shows live model info once the ML service has a trained model
- [ ] `/test-transactions`: submitting any Quick Template returns a risk score and (for high-risk templates) a fraud alert
- [ ] `/users` (as ADMIN): create, edit, reset password, and disable a user all work; nav item and page are hidden/blocked for non-ADMIN roles
- [ ] Logging in as a non-ADMIN role and visiting `/users` or `/test-transactions` (if not ANALYST/TESTER) shows Access Denied, not a crash
- [ ] Logging out clears the session and redirects to `/login`
- [ ] Stop the backend and confirm every page falls back to mock data with the demo banner visible
- [ ] `npm run build` succeeds with no type errors

## Troubleshooting

- **Demo data banner always showing**: confirm the backend/ML service are running and
  `NEXT_PUBLIC_API_BASE_URL` / `NEXT_PUBLIC_ML_SERVICE_URL` in `.env.local` match their actual ports.
  Environment variables are read at build time for `NEXT_PUBLIC_*` values — restart `npm run dev`
  after editing `.env.local`.
- **CORS errors in the browser console**: the Spring Boot backend must allow `http://localhost:3000`
  as an origin (or you're proxying through the same origin in production).
- **Type errors after pulling new backend DTO fields**: update `types/index.ts` to match; the API
  client uses those types for both real and mock data, so they must stay in sync.
- **Tailwind classes not applying**: this project uses Tailwind v4's CSS-first config — utility
  colors/radii are declared via `@theme inline` in `app/globals.css`, not `tailwind.config.ts`.
