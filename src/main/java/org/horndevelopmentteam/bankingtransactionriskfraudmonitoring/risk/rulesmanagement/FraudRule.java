package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums.RiskLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Database-driven configuration for one risk rule (see risk.rules.RiskRule implementations),
 * replacing what used to be hardcoded constants. ruleCode is the stable key each RiskRule impl
 * looks itself up by (e.g. "LARGE_AMOUNT_RULE") - renaming it would silently disconnect a rule
 * impl from its configuration, so treat it as append-only in practice.
 */
@Entity
@Table(name = "fraud_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String ruleId;

    @Column(unique = true, nullable = false, updatable = false)
    private String ruleCode;

    @Column(nullable = false)
    private String name;

    private String description;

    private String category;

    @Column(nullable = false)
    private Boolean enabled;

    /** Free-form string so it can hold either a number (e.g. "5000") or a CSV list (e.g. merchant
     * categories "CRYPTO,GAMBLING") depending on ruleType - each RiskRule impl parses what it expects. */
    private String thresholdValue;

    private String secondaryThresholdValue;

    @Column(nullable = false)
    private Integer scoreImpact;

    private Integer secondaryScoreImpact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudRuleType ruleType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private String updatedBy;

    public BigDecimal thresholdAsDecimal(BigDecimal fallback) {
        return parseDecimal(thresholdValue, fallback);
    }

    public BigDecimal secondaryThresholdAsDecimal(BigDecimal fallback) {
        return parseDecimal(secondaryThresholdValue, fallback);
    }

    private BigDecimal parseDecimal(String value, BigDecimal fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
