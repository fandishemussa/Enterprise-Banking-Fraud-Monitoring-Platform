package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user;

/** Minimal, non-sensitive user projection for populating assign/escalate dropdowns - deliberately
 * excludes everything UserResponse exposes beyond username/fullName/role, since callers of this
 * endpoint are not necessarily ADMIN. */
public record AssignableUserResponse(String username, String fullName, Role role) {

    public static AssignableUserResponse from(AppUser user) {
        return new AssignableUserResponse(user.getUsername(), user.getFullName(), user.getRole());
    }
}
