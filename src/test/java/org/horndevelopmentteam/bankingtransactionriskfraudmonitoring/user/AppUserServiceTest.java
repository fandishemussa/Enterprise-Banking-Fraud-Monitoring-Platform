package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppUserServiceTest {

    private AppUserRepository appUserRepository;
    private AppUserService appUserService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AppUser activeUser(String rawPassword) {
        return AppUser.builder()
                .id(1L)
                .userId("USER-1001")
                .username("analyst1")
                .email("analyst1@fraud.local")
                .fullName("Analyst One")
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(Role.ANALYST)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .failedLoginAttempts(0)
                .tokenVersion(0)
                .build();
    }

    @BeforeEach
    void setUp() {
        appUserRepository = mock(AppUserRepository.class);
        AuditLogService auditLogService = mock(AuditLogService.class);
        IdSequenceService idSequenceService = mock(IdSequenceService.class);
        MeterRegistry meterRegistry = new SimpleMeterRegistry();

        appUserService = new AppUserService(appUserRepository, passwordEncoder, idSequenceService,
                auditLogService, meterRegistry);
        ReflectionTestUtils.setField(appUserService, "maxFailedAttempts", 3);
        ReflectionTestUtils.setField(appUserService, "lockoutDurationMinutes", 15L);
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void authenticateSucceedsWithCorrectPassword() {
        AppUser user = activeUser("Correct@123");
        when(appUserRepository.findByUsername("analyst1")).thenReturn(Optional.of(user));

        AppUser result = appUserService.authenticate("analyst1", "Correct@123");

        assertThat(result.getUsername()).isEqualTo("analyst1");
        assertThat(result.getFailedLoginAttempts()).isZero();
    }

    @Test
    void authenticateRejectsWrongPasswordAndIncrementsFailedAttempts() {
        AppUser user = activeUser("Correct@123");
        when(appUserRepository.findByUsername("analyst1")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> appUserService.authenticate("analyst1", "wrong-password"))
                .isInstanceOf(UnauthorizedException.class);

        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void accountLocksAfterMaxFailedAttempts() {
        AppUser user = activeUser("Correct@123");
        when(appUserRepository.findByUsername("analyst1")).thenReturn(Optional.of(user));

        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> appUserService.authenticate("analyst1", "wrong-password"))
                    .isInstanceOf(UnauthorizedException.class);
        }

        assertThat(user.getStatus()).isEqualTo(UserStatus.LOCKED);
        assertThat(user.getLockedUntil()).isNotNull().isAfter(LocalDateTime.now());
    }

    @Test
    void lockedAccountRejectsEvenCorrectPasswordUntilLockoutExpires() {
        AppUser user = activeUser("Correct@123");
        user.setStatus(UserStatus.LOCKED);
        user.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        when(appUserRepository.findByUsername("analyst1")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> appUserService.authenticate("analyst1", "Correct@123"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("locked");
    }

    @Test
    void accountAutoUnlocksOncePastLockedUntil() {
        AppUser user = activeUser("Correct@123");
        user.setStatus(UserStatus.LOCKED);
        user.setLockedUntil(LocalDateTime.now().minusMinutes(1));
        user.setFailedLoginAttempts(3);
        when(appUserRepository.findByUsername("analyst1")).thenReturn(Optional.of(user));

        AppUser result = appUserService.authenticate("analyst1", "Correct@123");

        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.getFailedLoginAttempts()).isZero();
    }

    @Test
    void authenticateRejectsUnknownUsername() {
        when(appUserRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserService.authenticate("ghost", "whatever"))
                .isInstanceOf(UnauthorizedException.class);
    }
}
