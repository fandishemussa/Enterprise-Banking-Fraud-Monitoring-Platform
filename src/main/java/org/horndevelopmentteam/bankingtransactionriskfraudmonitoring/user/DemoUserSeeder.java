package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/** Dev-only accounts (one per non-admin role) so the assignment-based visibility rules
 * (FraudAlertService#getAlertsVisibleTo, InvestigationCaseService#getCasesVisibleTo) and the
 * escalate/assign dropdowns have more than just the admin account to exercise. Skipped entirely
 * outside the "dev" profile, and per-user idempotent (existing usernames are left untouched). */
@Component
@Profile("dev")
@Order(1)
@RequiredArgsConstructor
public class DemoUserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoUserSeeder.class);
    private static final String DEMO_PASSWORD = "Demo@12345";

    private record DemoUser(String username, String email, String fullName, Role role) {
    }

    private static final List<DemoUser> DEMO_USERS = List.of(
            new DemoUser("analyst1", "analyst1@fraud.local", "Amara Analyst", Role.ANALYST),
            new DemoUser("analyst2", "analyst2@fraud.local", "Noah Analyst", Role.ANALYST),
            new DemoUser("investigator1", "investigator1@fraud.local", "Ivy Investigator", Role.INVESTIGATOR),
            new DemoUser("viewer1", "viewer1@fraud.local", "Victor Viewer", Role.VIEWER),
            new DemoUser("tester1", "tester1@fraud.local", "Tess Tester", Role.TESTER)
    );

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final IdSequenceService idSequenceService;

    @Override
    public void run(String... args) {
        for (DemoUser demo : DEMO_USERS) {
            if (appUserRepository.existsByUsername(demo.username())) {
                continue;
            }
            LocalDateTime now = LocalDateTime.now();
            AppUser user = AppUser.builder()
                    .userId(idSequenceService.next("USER"))
                    .username(demo.username())
                    .email(demo.email())
                    .fullName(demo.fullName())
                    .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                    .role(demo.role())
                    .status(UserStatus.ACTIVE)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            appUserRepository.save(user);
            log.info("Seeded demo user: username='{}' role={}. Password: {} (dev only, see README).",
                    demo.username(), demo.role(), DEMO_PASSWORD);
        }
    }
}
