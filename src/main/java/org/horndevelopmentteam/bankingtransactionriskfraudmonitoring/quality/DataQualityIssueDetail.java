package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "data_quality_issue_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataQualityIssueDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_quality_result_id", nullable = false)
    private DataQualityResult dataQualityResult;

    private String recordIdentifier;

    @Lob
    @Column(nullable = false)
    private String issueDescription;

    @Column(nullable = false, updatable = false)
    private LocalDateTime detectedAt;
}
