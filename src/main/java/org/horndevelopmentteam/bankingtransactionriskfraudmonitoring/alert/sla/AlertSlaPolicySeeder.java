package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.AlertPriority;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Seeds the four default SLA policies (one per AlertPriority) on first startup - skipped
 * per-priority if a policy for it already exists, so re-running this never resets an admin's
 * edited response/resolution windows. */
@Component
@RequiredArgsConstructor
public class AlertSlaPolicySeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AlertSlaPolicySeeder.class);

    private final AlertSlaPolicyRepository alertSlaPolicyRepository;
    private final IdSequenceService idSequenceService;

    @Override
    public void run(String... args) {
        seedIfMissing(AlertPriority.CRITICAL, 15, 240);
        seedIfMissing(AlertPriority.HIGH, 60, 480);
        seedIfMissing(AlertPriority.MEDIUM, 240, 1440);
        seedIfMissing(AlertPriority.LOW, 1440, 4320);
    }

    private void seedIfMissing(AlertPriority priority, int responseTimeMinutes, int resolutionTimeMinutes) {
        if (alertSlaPolicyRepository.existsByPriority(priority)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        AlertSlaPolicy policy = AlertSlaPolicy.builder()
                .policyId(idSequenceService.next("SLA"))
                .priority(priority)
                .responseTimeMinutes(responseTimeMinutes)
                .resolutionTimeMinutes(resolutionTimeMinutes)
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        alertSlaPolicyRepository.save(policy);
        log.info("Seeded default SLA policy: {} ({})", policy.getPolicyId(), priority);
    }
}
