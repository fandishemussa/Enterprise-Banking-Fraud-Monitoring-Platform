import type { AppUser, Role } from "@/types";

const TOKEN_KEY = "fraud_monitoring_token";
const USER_KEY = "fraud_monitoring_user";

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return window.localStorage.getItem(TOKEN_KEY);
}

export function getStoredUser(): AppUser | null {
  if (typeof window === "undefined") return null;
  const raw = window.localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AppUser;
  } catch {
    return null;
  }
}

export function setSession(token: string, user: AppUser): void {
  window.localStorage.setItem(TOKEN_KEY, token);
  window.localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearSession(): void {
  window.localStorage.removeItem(TOKEN_KEY);
  window.localStorage.removeItem(USER_KEY);
}

interface JwtPayload {
  sub: string;
  role: Role;
  exp: number;
}

/** Decodes the JWT payload client-side (no signature verification - the backend is the source
 * of truth for every request; this is only used to pre-emptively detect an expired token). */
export function decodeToken(token: string): JwtPayload | null {
  try {
    const [, payload] = token.split(".");
    const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(
      atob(normalized)
        .split("")
        .map((c) => "%" + c.charCodeAt(0).toString(16).padStart(2, "0"))
        .join(""),
    );
    return JSON.parse(json) as JwtPayload;
  } catch {
    return null;
  }
}

export function isTokenExpired(token: string): boolean {
  const payload = decodeToken(token);
  if (!payload) return true;
  return Date.now() >= payload.exp * 1000;
}

export const ROLE_PERMISSIONS: Record<Role, { canManageUsers: boolean; canSubmitTestTransactions: boolean }> = {
  ADMIN: { canManageUsers: true, canSubmitTestTransactions: true },
  ANALYST: { canManageUsers: false, canSubmitTestTransactions: true },
  INVESTIGATOR: { canManageUsers: false, canSubmitTestTransactions: false },
  VIEWER: { canManageUsers: false, canSubmitTestTransactions: false },
  TESTER: { canManageUsers: false, canSubmitTestTransactions: true },
};
