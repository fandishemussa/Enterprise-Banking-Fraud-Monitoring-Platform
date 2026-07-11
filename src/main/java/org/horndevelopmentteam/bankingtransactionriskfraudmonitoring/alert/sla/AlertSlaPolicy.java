package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla;

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
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.AlertPriority;

import java.time.LocalDateTime;

/** One row per AlertPriority (reused rather than introducing a parallel "riskLevel" concept -
 * FraudAlert.priority already mirrors RiskLevel 1:1). */
@Entity
@Table(name = "alert_sla_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertSlaPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String policyId;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private AlertPriority priority;

    @Column(nullable = false)
    private Integer responseTimeMinutes;

    @Column(nullable = false)
    private Integer resolutionTimeMinutes;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
