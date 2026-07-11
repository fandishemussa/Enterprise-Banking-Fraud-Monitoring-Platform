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
@Table(name = "alert_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String alertId;

    @Column(nullable = false, updatable = false)
    private String oldStatus;

    @Column(nullable = false, updatable = false)
    private String newStatus;

    @Column(nullable = false, updatable = false)
    private String changedBy;

    private String reason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
