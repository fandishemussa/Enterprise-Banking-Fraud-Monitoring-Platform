package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.crypto.DeterministicEncryptedStringConverter;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String userId;

    @Column(unique = true, nullable = false)
    private String username;

    // Deterministic encryption - looked up by exact value (AppUserRepository.existsByEmail) and
    // has a UNIQUE constraint; random-IV encryption would break both.
    @Convert(converter = DeterministicEncryptedStringConverter.class)
    @Column(unique = true, nullable = false, length = 500)
    private String email;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;

    @Column(nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;

    /** Set when failedLoginAttempts crosses the threshold; account auto-unlocks once this passes,
     * without needing an admin to flip the status back manually. */
    private LocalDateTime lockedUntil;

    /** Bumped on logout/password change/reset so previously-issued JWTs (which embed this value)
     * are rejected immediately - the closest thing to revocation a stateless JWT scheme allows
     * without a server-side session/blacklist store. */
    @Column(nullable = false)
    @Builder.Default
    private int tokenVersion = 0;
}
