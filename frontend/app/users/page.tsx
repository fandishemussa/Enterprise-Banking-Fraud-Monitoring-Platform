"use client";

import { useState } from "react";
import { UserPlus, AlertCircle } from "lucide-react";
import { PageHeader } from "@/components/layout/page-header";
import { SectionCard } from "@/components/shared/section-card";
import { UsersTable } from "@/components/tables/users-table";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { AccessDenied } from "@/components/shared/access-denied";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogClose, DialogFooter } from "@/components/ui/dialog";
import { useUsers } from "@/hooks/use-users";
import { useAuth } from "@/hooks/use-auth";
import { createUser, resetUserPassword, updateUser, updateUserStatus } from "@/lib/api";
import type { AppUser, Role } from "@/types";

const ROLES: Role[] = ["ADMIN", "ANALYST", "INVESTIGATOR", "VIEWER", "TESTER"];

const ROLE_CAPABILITIES: Record<Role, string> = {
  ADMIN: "Full platform control: manage users and roles, create/edit/enable/disable fraud rules, manage SLA policies, plus everything below.",
  ANALYST: "Work the fraud queue: assign/escalate/update alerts, create and manage investigation cases, upload CSV batches, publish test/streaming transactions, view all monitoring pages.",
  INVESTIGATOR: "Work assigned alerts and cases: update status, assign/escalate, add case notes and timeline entries. Cannot create cases, manage fraud rules, SLA policies, or users.",
  VIEWER: "Read-only across every dashboard and record (transactions, alerts, cases, customers, risk scores, audit logs). Cannot change anything.",
  TESTER: "Generate synthetic test transactions and upload CSV batches / publish streaming test data for QA and demos. Read access elsewhere; cannot manage rules, SLA, or users.",
};

export default function UsersPage() {
  const { user, hasRole } = useAuth();
  const { data: users, isLoading, error, refetch } = useUsers();

  const [createOpen, setCreateOpen] = useState(false);
  const [editUser, setEditUser] = useState<AppUser | null>(null);
  const [resetUser, setResetUser] = useState<AppUser | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  if (!hasRole("ADMIN")) {
    return (
      <div className="space-y-6">
        <PageHeader title="Users" description="Manage platform users and roles" />
        <AccessDenied message="Only administrators can manage users." />
      </div>
    );
  }

  async function handleToggleStatus(target: AppUser) {
    setActionError(null);
    try {
      await updateUserStatus(target.userId, target.status === "ACTIVE" ? "DISABLED" : "ACTIVE");
      refetch();
    } catch (err) {
      setActionError(err instanceof Error ? err.message : "Failed to update user status");
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Users"
        description="Manage platform users and roles"
        action={
          <Button onClick={() => setCreateOpen(true)}>
            <UserPlus className="mr-2 h-4 w-4" />
            New User
          </Button>
        }
      />

      {actionError && (
        <div className="flex items-start gap-2 rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-xs text-destructive">
          <AlertCircle className="mt-0.5 h-3.5 w-3.5 shrink-0" />
          <span>{actionError}</span>
        </div>
      )}

      <SectionCard title="Role Capabilities" description="What each role can see and do on this platform">
        <ul className="divide-y divide-border">
          {ROLES.map((r) => (
            <li key={r} className="flex flex-col gap-1 py-2.5 sm:flex-row sm:items-baseline sm:gap-4">
              <span className="w-28 shrink-0 text-sm font-semibold text-foreground">{r}</span>
              <span className="text-xs text-muted-foreground">{ROLE_CAPABILITIES[r]}</span>
            </li>
          ))}
        </ul>
      </SectionCard>

      {isLoading ? (
        <LoadingState label="Loading users..." />
      ) : error ? (
        <ErrorState message={error} onRetry={refetch} />
      ) : (
        <UsersTable
          users={users ?? []}
          currentUsername={user?.username ?? ""}
          onEdit={setEditUser}
          onResetPassword={setResetUser}
          onToggleStatus={handleToggleStatus}
        />
      )}

      <CreateUserDialog open={createOpen} onOpenChange={setCreateOpen} onCreated={refetch} />
      <EditUserDialog user={editUser} onOpenChange={() => setEditUser(null)} onUpdated={refetch} />
      <ResetPasswordDialog user={resetUser} onOpenChange={() => setResetUser(null)} onReset={refetch} />
    </div>
  );
}

function CreateUserDialog({
  open,
  onOpenChange,
  onCreated,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onCreated: () => void;
}) {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [fullName, setFullName] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState<Role>("VIEWER");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  function reset() {
    setUsername("");
    setEmail("");
    setFullName("");
    setPassword("");
    setRole("VIEWER");
    setError(null);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      await createUser({ username, email, fullName, password, role });
      reset();
      onOpenChange(false);
      onCreated();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create user");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Create User</DialogTitle>
          <DialogClose onClick={() => onOpenChange(false)} />
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-3">
          <Input placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} required />
          <Input type="email" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          <Input placeholder="Full name" value={fullName} onChange={(e) => setFullName(e.target.value)} required />
          <Input
            type="password"
            placeholder="Temporary password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          <Select value={role} onChange={(e) => setRole(e.target.value as Role)}>
            {ROLES.map((r) => (
              <option key={r} value={r}>
                {r}
              </option>
            ))}
          </Select>
          <p className="text-xs text-muted-foreground">{ROLE_CAPABILITIES[role]}</p>
          {error && <p className="text-xs text-destructive">{error}</p>}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Creating..." : "Create User"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

function EditUserDialog({
  user,
  onOpenChange,
  onUpdated,
}: {
  user: AppUser | null;
  onOpenChange: (open: boolean) => void;
  onUpdated: () => void;
}) {
  return (
    <Dialog open={user !== null} onOpenChange={(o) => !o && onOpenChange(false)}>
      {user && <EditUserForm key={user.userId} user={user} onOpenChange={onOpenChange} onUpdated={onUpdated} />}
    </Dialog>
  );
}

function EditUserForm({
  user,
  onOpenChange,
  onUpdated,
}: {
  user: AppUser;
  onOpenChange: (open: boolean) => void;
  onUpdated: () => void;
}) {
  const [fullName, setFullName] = useState(user.fullName);
  const [email, setEmail] = useState(user.email);
  const [role, setRole] = useState<Role>(user.role);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  return (
    <DialogContent>
      <DialogHeader>
        <DialogTitle>Edit User - {user.username}</DialogTitle>
        <DialogClose onClick={() => onOpenChange(false)} />
      </DialogHeader>
      <form
        onSubmit={async (e) => {
          e.preventDefault();
          setError(null);
          setIsSubmitting(true);
          try {
            await updateUser(user.userId, { fullName, email, role });
            onOpenChange(false);
            onUpdated();
          } catch (err) {
            setError(err instanceof Error ? err.message : "Failed to update user");
          } finally {
            setIsSubmitting(false);
          }
        }}
        className="space-y-3"
      >
        <Input placeholder="Full name" value={fullName} onChange={(e) => setFullName(e.target.value)} />
        <Input type="email" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} />
        <Select value={role} onChange={(e) => setRole(e.target.value as Role)}>
          {ROLES.map((r) => (
            <option key={r} value={r}>
              {r}
            </option>
          ))}
        </Select>
        <p className="text-xs text-muted-foreground">{ROLE_CAPABILITIES[role]}</p>
        {error && <p className="text-xs text-destructive">{error}</p>}
        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Saving..." : "Save Changes"}
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  );
}

function ResetPasswordDialog({
  user,
  onOpenChange,
  onReset,
}: {
  user: AppUser | null;
  onOpenChange: (open: boolean) => void;
  onReset: () => void;
}) {
  const [newPassword, setNewPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  return (
    <Dialog open={user !== null} onOpenChange={(o) => !o && onOpenChange(false)}>
      {user && (
        <DialogContent key={user.userId}>
          <DialogHeader>
            <DialogTitle>Reset Password - {user.username}</DialogTitle>
            <DialogClose onClick={() => onOpenChange(false)} />
          </DialogHeader>
          <form
            onSubmit={async (e) => {
              e.preventDefault();
              setError(null);
              setIsSubmitting(true);
              try {
                await resetUserPassword(user.userId, newPassword);
                setNewPassword("");
                onOpenChange(false);
                onReset();
              } catch (err) {
                setError(err instanceof Error ? err.message : "Failed to reset password");
              } finally {
                setIsSubmitting(false);
              }
            }}
            className="space-y-3"
          >
            <Input
              type="password"
              placeholder="New password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
            />
            {error && <p className="text-xs text-destructive">{error}</p>}
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                Cancel
              </Button>
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? "Resetting..." : "Reset Password"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      )}
    </Dialog>
  );
}
