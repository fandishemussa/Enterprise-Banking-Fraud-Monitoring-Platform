package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums.RiskLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Seeds the six default fraud rules on first startup - skipped per-rule if a rule with that
 * ruleCode already exists, so re-running this never resets an admin's threshold edits. */
@Component
@RequiredArgsConstructor
public class FraudRuleSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FraudRuleSeeder.class);

    private final FraudRuleRepository fraudRuleRepository;
    private final IdSequenceService idSequenceService;

    @Override
    public void run(String... args) {
        seedIfMissing("LARGE_AMOUNT_RULE", "Large Amount", "Flags transactions above a large-amount threshold",
                "AMOUNT", "5000", "10000", 30, 50, RiskLevel.HIGH, FraudRuleType.AMOUNT);
        seedIfMissing("NEW_DEVICE_RULE", "New Device", "Flags the first transaction from a device not seen before for this customer",
                "DEVICE", null, null, 20, null, RiskLevel.MEDIUM, FraudRuleType.DEVICE);
        seedIfMissing("NEW_COUNTRY_RULE", "New Country", "Flags the first transaction from a country not seen before for this customer",
                "COUNTRY", null, null, 25, null, RiskLevel.HIGH, FraudRuleType.COUNTRY);
        seedIfMissing("HIGH_FREQUENCY_RULE", "High Frequency", "Flags more than the threshold count of transactions in a 10-minute window",
                "FREQUENCY", "5", null, 35, null, RiskLevel.HIGH, FraudRuleType.FREQUENCY);
        seedIfMissing("HIGH_RISK_MERCHANT_RULE", "High-Risk Merchant", "Flags transactions in high-risk merchant categories",
                "MERCHANT", "CRYPTO,GAMBLING,HIGH_RISK_TRANSFER", null, 25, null, RiskLevel.HIGH, FraudRuleType.MERCHANT);
        seedIfMissing("UNUSUAL_HOUR_RULE", "Unusual Hour", "Flags transactions occurring during unusual overnight hours",
                "TIME", "0", "5", 15, null, RiskLevel.MEDIUM, FraudRuleType.TIME);
        seedIfMissing("SHARED_DEVICE_IP_RULE", "Shared Device/IP",
                "Flags transactions whose device ID or IP address was already used by a different customer - a fraud ring signal",
                "DEVICE", null, null, 30, null, RiskLevel.HIGH, FraudRuleType.DEVICE);
    }

    private void seedIfMissing(String ruleCode, String name, String description, String category,
                                 String thresholdValue, String secondaryThresholdValue,
                                 int scoreImpact, Integer secondaryScoreImpact,
                                 RiskLevel severity, FraudRuleType ruleType) {
        if (fraudRuleRepository.existsByRuleCode(ruleCode)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        FraudRule rule = FraudRule.builder()
                .ruleId(idSequenceService.next("RULE"))
                .ruleCode(ruleCode)
                .name(name)
                .description(description)
                .category(category)
                .enabled(true)
                .thresholdValue(thresholdValue)
                .secondaryThresholdValue(secondaryThresholdValue)
                .scoreImpact(scoreImpact)
                .secondaryScoreImpact(secondaryScoreImpact)
                .severity(severity)
                .ruleType(ruleType)
                .createdAt(now)
                .updatedAt(now)
                .updatedBy("system")
                .build();
        fraudRuleRepository.save(rule);
        log.info("Seeded default fraud rule: {} ({})", rule.getRuleId(), ruleCode);
    }
}
