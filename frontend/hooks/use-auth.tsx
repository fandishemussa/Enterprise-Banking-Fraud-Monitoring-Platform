"use client";

import { createContext, useCallback, useContext, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import type { AppUser, Role } from "@/types";
import { clearSession, getStoredUser, getToken, isTokenExpired, ROLE_PERMISSIONS, setSession } from "@/lib/auth";
import { login as loginApi } from "@/lib/api";

interface AuthContextValue {
  user: AppUser | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  hasRole: (...roles: Role[]) => boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AppUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const token = getToken();
    const storedUser = getStoredUser();
    if (token && storedUser && !isTokenExpired(token)) {
      setUser(storedUser);
    } else if (token) {
      clearSession();
    }
    setIsLoading(false);
  }, []);

  useEffect(() => {
    function handleSessionExpired() {
      setUser(null);
      router.push("/login");
    }
    window.addEventListener("auth:session-expired", handleSessionExpired);
    return () => window.removeEventListener("auth:session-expired", handleSessionExpired);
  }, [router]);

  const login = useCallback(async (username: string, password: string) => {
    const response = await loginApi(username, password);
    setSession(response.token, response.user);
    setUser(response.user);
  }, []);

  const logout = useCallback(() => {
    clearSession();
    setUser(null);
    router.push("/login");
  }, [router]);

  const hasRole = useCallback((...roles: Role[]) => !!user && roles.includes(user.role), [user]);

  return (
    <AuthContext.Provider value={{ user, isLoading, isAuthenticated: user !== null, login, logout, hasRole }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
}

export function canManageUsers(role: Role): boolean {
  return ROLE_PERMISSIONS[role].canManageUsers;
}

export function canSubmitTestTransactions(role: Role): boolean {
  return ROLE_PERMISSIONS[role].canSubmitTestTransactions;
}
