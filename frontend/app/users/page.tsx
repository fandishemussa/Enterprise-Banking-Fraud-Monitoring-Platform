"use client";

import { useState } from "react";
import { UserPlus, AlertCircle } from "lucide-react";
import { PageHeader } from "@/components/layout/page-header";
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
