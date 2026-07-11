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

import java.time.LocalDateTime;

@Entity
@Table(name = "alert_sla_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertSlaResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String alertId;

    @Column(nullable = false, updatable = false)
    private String policyId;

    @Column(nullable = false)
    private LocalDateTime responseDeadline;

    @Column(nullable = false)
    private LocalDateTime resolutionDeadline;

    private LocalDateTime firstAcknowledgedAt;

    private LocalDateTime resolvedAt;

    @Column(nullable = false)
    private Boolean responseBreached;

    @Column(nullable = false)
    private Boolean resolutionBreached;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlaStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
