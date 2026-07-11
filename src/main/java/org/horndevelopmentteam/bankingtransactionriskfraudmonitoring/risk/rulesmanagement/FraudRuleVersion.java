package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** One row per change to a FraudRule - a lightweight audit trail specific to rule configuration,
 * separate from the general AuditLog so the full before/after JSON is easy to diff and display. */
@Entity
@Table(name = "fraud_rule_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudRuleVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ruleId;

    @Column(nullable = false)
    private Integer versionNumber;

    @Column(columnDefinition = "text")
    private String oldConfigJson;

    @Column(nullable = false, columnDefinition = "text")
    private String newConfigJson;

    @Column(nullable = false)
    private String changedBy;

    private String changeReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
