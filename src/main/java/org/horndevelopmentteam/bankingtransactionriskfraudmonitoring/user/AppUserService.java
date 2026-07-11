package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.BadRequestException;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ResourceNotFoundException;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final IdSequenceService idSequenceService;
    private final AuditLogService auditLogService;
    private final MeterRegistry meterRegistry;

    @Value("${security.login.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${security.login.lockout-duration-minutes:15}")
    private long lockoutDurationMinutes;

    @Transactional
    public UserResponse createUser(CreateUserRequest request, String actingUsername) {
        if (appUserRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username already in use: " + request.username());
        }
        if (appUserRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already in use: " + request.email());
        }

        LocalDateTime now = LocalDateTime.now();
        AppUser user = AppUser.builder()
                .userId(idSequenceService.next("USER"))
                .username(request.username())
                .email(request.email())
                .fullName(request.fullName())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .status(UserStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
        AppUser saved = appUserRepository.save(user);

        auditLogService.record(
                AuditEventType.USER_CREATED,
                "AppUser",
                saved.getUserId(),
                null,
                saved.getRole().name(),
                "User " + saved.getUserId() + " (" + saved.getUsername() + ") created by " + actingUsername
        );

        return UserResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return appUserRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    /** Active users only, roles that can be assigned an alert/case per the platform's RBAC model
     * (ADMIN, ANALYST, INVESTIGATOR) - VIEWER/TESTER accounts don't work alerts so they're excluded
     * from the assignment dropdown by design, not an oversight. */
    @Transactional(readOnly = true)
    public List<AssignableUserResponse> getAssignableUsers() {
        return appUserRepository.findByStatusOrderByUsername(UserStatus.ACTIVE).stream()
                .filter(user -> user.getRole() == Role.ADMIN || user.getRole() == Role.ANALYST || user.getRole() == Role.INVESTIGATOR)
                .map(AssignableUserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByPublicId(String userId) {
        return UserResponse.from(findByPublicIdOrThrow(userId));
    }

    @Transactional
    public UserResponse updateUser(String userId, UpdateUserRequest request, String actingUsername) {
        AppUser user = findByPublicIdOrThrow(userId);
        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.email() != null) {
            user.setEmail(request.email());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        user.setUpdatedAt(LocalDateTime.now());
        AppUser saved = appUserRepository.save(user);

        auditLogService.record(
                AuditEventType.USER_UPDATED,
                "AppUser",
                saved.getUserId(),
                null,
                saved.getRole().name(),
                "User " + saved.getUserId() + " updated by " + actingUsername
        );

        return UserResponse.from(saved);
    }

    @Transactional
    public UserResponse updateStatus(String userId, UserStatus status, String actingUsername) {
        AppUser user = findByPublicIdOrThrow(userId);
        UserStatus oldStatus = user.getStatus();
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        AppUser saved = appUserRepository.save(user);

        auditLogService.record(
                AuditEventType.USER_DISABLED,
                "AppUser",
                saved.getUserId(),
                oldStatus.name(),
                status.name(),
                "User " + saved.getUserId() + " status changed from " + oldStatus + " to " + status + " by " + actingUsername
        );

        return UserResponse.from(saved);
    }

    /** Soft-disable only - user rows are never physically deleted. */
    @Transactional
    public UserResponse disableUser(String userId, String actingUsername) {
        return updateStatus(userId, UserStatus.DISABLED, actingUsername);
    }

    @Transactional
    public UserResponse resetPassword(String userId, String newPassword, String actingUsername) {
        AppUser user = findByPublicIdOrThrow(userId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        user.setTokenVersion(user.getTokenVersion() + 1);
        AppUser saved = appUserRepository.save(user);

        auditLogService.record(
                AuditEventType.USER_PASSWORD_RESET,
                "AppUser",
                saved.getUserId(),
                null,
                null,
                "Password reset for user " + saved.getUserId() + " by " + actingUsername
        );

        return UserResponse.from(saved);
    }

    @Transactional
    public void changeOwnPassword(String username, String currentPassword, String newPassword) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        user.setTokenVersion(user.getTokenVersion() + 1);
        appUserRepository.save(user);

        auditLogService.record(
                AuditEventType.USER_PASSWORD_RESET,
                "AppUser",
                user.getUserId(),
                null,
                null,
                "User " + user.getUserId() + " changed their own password"
        );
    }

    /** Validates credentials and records LOGIN_SUCCESS/LOGIN_FAILED; never throws for bad credentials
     * without auditing the attempt first. Auto-locks the account for lockoutDurationMinutes after
     * maxFailedAttempts consecutive failures, and auto-unlocks once that window has passed - no
     * admin intervention needed for the common case of a user mistyping their password repeatedly.
     * noRollbackFor is required here: this method intentionally saves the incremented
     * failedLoginAttempts/lockout state and then throws UnauthorizedException in the same
     * transaction to reject the request - without it, Spring's default rollback-on-RuntimeException
     * would silently undo that save, and failed attempts would never actually accumulate. */
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public AppUser authenticate(String username, String password) {
        AppUser user = appUserRepository.findByUsername(username).orElse(null);

        if (user == null) {
            auditLogService.record(
                    AuditEventType.LOGIN_FAILED, "AppUser", username, null, null,
                    "Login failed for username " + username
            );
            meterRegistry.counter("login_attempts_total", "result", "failure").increment();
            throw new UnauthorizedException("Invalid username or password");
        }

        if (user.getStatus() == UserStatus.LOCKED && user.getLockedUntil() != null
                && LocalDateTime.now().isAfter(user.getLockedUntil())) {
            user.setStatus(UserStatus.ACTIVE);
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            auditLogService.record(
                    AuditEventType.LOGIN_FAILED, "AppUser", user.getUserId(), null, null,
                    "Login blocked for " + user.getUsername() + " - account status is " + user.getStatus()
            );
            appUserRepository.save(user);
            meterRegistry.counter("login_attempts_total", "result", "blocked").increment();
            throw new UnauthorizedException("Account is " + user.getStatus().name().toLowerCase()
                    + (user.getLockedUntil() != null ? " until " + user.getLockedUntil() : ""));
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            String description = "Login failed for username " + username;
            if (user.getFailedLoginAttempts() >= maxFailedAttempts) {
                user.setStatus(UserStatus.LOCKED);
                user.setLockedUntil(LocalDateTime.now().plus(Duration.ofMinutes(lockoutDurationMinutes)));
                description += " - account locked after " + user.getFailedLoginAttempts() + " failed attempts";
            }
            appUserRepository.save(user);
            auditLogService.record(AuditEventType.LOGIN_FAILED, "AppUser", user.getUserId(), null, null, description);
            meterRegistry.counter("login_attempts_total", "result", "failure").increment();
            throw new UnauthorizedException("Invalid username or password");
        }

        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        appUserRepository.save(user);
        meterRegistry.counter("login_attempts_total", "result", "success").increment();

        auditLogService.record(
                AuditEventType.LOGIN_SUCCESS,
                "AppUser",
                user.getUserId(),
                null,
                null,
                "User " + user.getUsername() + " logged in successfully"
        );

        return user;
    }

    /** Bumps tokenVersion so every previously-issued JWT for this user is rejected on its next use. */
    @Transactional
    public void invalidateAllSessions(String username) {
        AppUser user = findByUsernameOrThrow(username);
        user.setTokenVersion(user.getTokenVersion() + 1);
        appUserRepository.save(user);
    }

    public AppUser findByPublicIdOrThrow(String userId) {
        return appUserRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    public AppUser findByUsernameOrThrow(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
