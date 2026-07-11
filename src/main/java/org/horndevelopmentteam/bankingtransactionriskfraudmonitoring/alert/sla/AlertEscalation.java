package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla;

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

@Entity
@Table(name = "alert_escalations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertEscalation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String escalationId;

    @Column(nullable = false, updatable = false)
    private String alertId;

    @Column(nullable = false, updatable = false)
    private String escalatedFrom;

    @Column(nullable = false, updatable = false)
    private String escalatedTo;

    private String reason;

    @Column(nullable = false, updatable = false)
    private Integer escalationLevel;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
