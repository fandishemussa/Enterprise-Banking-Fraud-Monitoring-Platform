package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.BadRequestException;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** CRUD + enable/disable + version history for FraudRule. Threshold/score reads used by the actual
 * scoring engine live in FraudRuleConfigService, not here - this is the admin-facing management
 * surface only. */
@Service
@RequiredArgsConstructor
public class FraudRuleService {

    private static final String SYSTEM_USER = "system";

    private final FraudRuleRepository fraudRuleRepository;
    private final FraudRuleVersionRepository fraudRuleVersionRepository;
    private final IdSequenceService idSequenceService;
    private final AuditLogService auditLogService;

    // Not Spring-managed: this project uses spring-boot-starter-webmvc rather than the full
    // spring-boot-starter-web + spring-boot-starter-json combo, so an autoconfigured ObjectMapper
    // bean isn't guaranteed to exist - and this one is only used for an internal version-history
    // snapshot string, not for actual HTTP (de)serialization, so it doesn't need Spring's config anyway.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public List<FraudRuleResponse> getAll() {
        return fraudRuleRepository.findAll().stream().map(FraudRuleResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public FraudRuleResponse getByRuleId(String ruleId) {
        return FraudRuleResponse.from(findOrThrow(ruleId));
    }

    @Transactional
    public FraudRuleResponse create(FraudRuleRequest request, String actingUsername) {
        if (fraudRuleRepository.existsByRuleCode(request.ruleCode())) {
            throw new BadRequestException("A fraud rule with ruleCode already exists: " + request.ruleCode());
        }

        LocalDateTime now = LocalDateTime.now();
        FraudRule rule = FraudRule.builder()
                .ruleId(idSequenceService.next("RULE"))
                .ruleCode(request.ruleCode())
                .name(request.name())
                .description(request.description())
                .category(request.category())
                .enabled(request.enabled() == null || request.enabled())
                .thresholdValue(request.thresholdValue())
                .secondaryThresholdValue(request.secondaryThresholdValue())
                .scoreImpact(request.scoreImpact())
                .secondaryScoreImpact(request.secondaryScoreImpact())
                .severity(request.severity())
                .ruleType(request.ruleType())
                .createdAt(now)
                .updatedAt(now)
                .updatedBy(actingUsername)
                .build();
        FraudRule saved = fraudRuleRepository.save(rule);

        recordVersion(saved.getRuleId(), null, saved, actingUsername,
                request.changeReason() != null ? request.changeReason() : "Rule created");

        auditLogService.record(AuditEventType.FRAUD_RULE_CREATED, "FraudRule", saved.getRuleId(),
                null, saved.getRuleCode(), "Fraud rule " + saved.getRuleId() + " (" + saved.getRuleCode() + ") created");

        return FraudRuleResponse.from(saved);
    }

    @Transactional
    public FraudRuleResponse update(String ruleId, FraudRuleRequest request, String actingUsername) {
        FraudRule rule = findOrThrow(ruleId);
        String oldConfigJson = toJson(rule);

        rule.setName(request.name());
        rule.setDescription(request.description());
        rule.setCategory(request.category());
        if (request.enabled() != null) rule.setEnabled(request.enabled());
        rule.setThresholdValue(request.thresholdValue());
        rule.setSecondaryThresholdValue(request.secondaryThresholdValue());
        rule.setScoreImpact(request.scoreImpact());
        rule.setSecondaryScoreImpact(request.secondaryScoreImpact());
        rule.setSeverity(request.severity());
        rule.setRuleType(request.ruleType());
        rule.setUpdatedAt(LocalDateTime.now());
        rule.setUpdatedBy(actingUsername);
        FraudRule saved = fraudRuleRepository.save(rule);

        recordVersionRaw(saved.getRuleId(), oldConfigJson, toJson(saved), actingUsername,
                request.changeReason() != null ? request.changeReason() : "Rule updated");

        auditLogService.record(AuditEventType.FRAUD_RULE_UPDATED, "FraudRule", saved.getRuleId(),
                oldConfigJson, toJson(saved), "Fraud rule " + saved.getRuleId() + " updated");

        return FraudRuleResponse.from(saved);
    }

    @Transactional
    public FraudRuleResponse setEnabled(String ruleId, boolean enabled, String actingUsername) {
        FraudRule rule = findOrThrow(ruleId);
        if (Boolean.TRUE.equals(rule.getEnabled()) == enabled) {
            return FraudRuleResponse.from(rule);
        }
        String oldConfigJson = toJson(rule);
        rule.setEnabled(enabled);
        rule.setUpdatedAt(LocalDateTime.now());
        rule.setUpdatedBy(actingUsername);
        FraudRule saved = fraudRuleRepository.save(rule);

        recordVersionRaw(saved.getRuleId(), oldConfigJson, toJson(saved), actingUsername,
                enabled ? "Rule enabled" : "Rule disabled");

        auditLogService.record(
                enabled ? AuditEventType.FRAUD_RULE_ENABLED : AuditEventType.FRAUD_RULE_DISABLED,
                "FraudRule", saved.getRuleId(), oldConfigJson, toJson(saved),
                "Fraud rule " + saved.getRuleId() + " " + (enabled ? "enabled" : "disabled"));

        return FraudRuleResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<FraudRuleVersionResponse> getVersions(String ruleId) {
        findOrThrow(ruleId);
        return fraudRuleVersionRepository.findByRuleIdOrderByVersionNumberDesc(ruleId).stream()
                .map(FraudRuleVersionResponse::from)
                .toList();
    }

    private FraudRule findOrThrow(String ruleId) {
        return fraudRuleRepository.findByRuleId(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Fraud rule not found: " + ruleId));
    }

    private void recordVersion(String ruleId, String oldConfigJson, FraudRule newRule, String changedBy, String reason) {
        recordVersionRaw(ruleId, oldConfigJson, toJson(newRule), changedBy, reason);
    }

    private void recordVersionRaw(String ruleId, String oldConfigJson, String newConfigJson, String changedBy, String reason) {
        int nextVersion = fraudRuleVersionRepository.countByRuleId(ruleId) + 1;
        FraudRuleVersion version = FraudRuleVersion.builder()
                .ruleId(ruleId)
                .versionNumber(nextVersion)
                .oldConfigJson(oldConfigJson)
                .newConfigJson(newConfigJson)
                .changedBy(changedBy != null ? changedBy : SYSTEM_USER)
                .changeReason(reason)
                .createdAt(LocalDateTime.now())
                .build();
        fraudRuleVersionRepository.save(version);
        auditLogService.record(AuditEventType.FRAUD_RULE_VERSION_CREATED, "FraudRuleVersion", ruleId,
                null, "v" + nextVersion, "Fraud rule version " + nextVersion + " recorded for " + ruleId);
    }

    private String toJson(FraudRule rule) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "ruleCode", rule.getRuleCode(),
                    "enabled", Boolean.TRUE.equals(rule.getEnabled()),
                    "thresholdValue", rule.getThresholdValue() != null ? rule.getThresholdValue() : "",
                    "secondaryThresholdValue", rule.getSecondaryThresholdValue() != null ? rule.getSecondaryThresholdValue() : "",
                    "scoreImpact", rule.getScoreImpact(),
                    "secondaryScoreImpact", rule.getSecondaryScoreImpact() != null ? rule.getSecondaryScoreImpact() : 0,
                    "severity", rule.getSeverity().name()
            ));
        } catch (Exception ex) {
            return "{}";
        }
    }
}
