package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Creates the one and only default admin account on first startup - skipped entirely once any
 * ADMIN user already exists, so re-running this never resets an admin's password or duplicates users. */
@Component
@RequiredArgsConstructor
public class DefaultAdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultAdminSeeder.class);

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final IdSequenceService idSequenceService;

    @Value("${admin.default-username}")
    private String defaultUsername;

    @Value("${admin.default-email}")
    private String defaultEmail;

    @Value("${admin.default-password}")
    private String defaultPassword;

    @Value("${admin.default-full-name}")
    private String defaultFullName;

    @Override
    public void run(String... args) {
        if (appUserRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        AppUser admin = AppUser.builder()
                .userId(idSequenceService.next("USER"))
                .username(defaultUsername)
                .email(defaultEmail)
                .fullName(defaultFullName)
                .passwordHash(passwordEncoder.encode(defaultPassword))
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
        appUserRepository.save(admin);

        log.info("Default admin user created: username='{}'. See README for local dev credentials.", defaultUsername);
    }
}
