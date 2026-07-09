package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.BadRequestException;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ResourceNotFoundException;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final IdSequenceService idSequenceService;
    private final AuditLogService auditLogService;

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
     * without auditing the attempt first. */
    @Transactional
    public AppUser authenticate(String username, String password) {
        AppUser user = appUserRepository.findByUsername(username).orElse(null);

        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            auditLogService.record(
                    AuditEventType.LOGIN_FAILED,
                    "AppUser",
                    username,
                    null,
                    null,
                    "Login failed for username " + username
            );
            throw new UnauthorizedException("Invalid username or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            auditLogService.record(
                    AuditEventType.LOGIN_FAILED,
                    "AppUser",
                    user.getUserId(),
                    null,
                    null,
                    "Login blocked for " + user.getUsername() + " - account status is " + user.getStatus()
            );
            throw new UnauthorizedException("Account is " + user.getStatus().name().toLowerCase());
        }

        user.setLastLoginAt(LocalDateTime.now());
        appUserRepository.save(user);

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

    public AppUser findByPublicIdOrThrow(String userId) {
        return appUserRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    public AppUser findByUsernameOrThrow(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
