package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "data_quality_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataQualityResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_quality_run_id", nullable = false)
    private DataQualityRun dataQualityRun;

    @Column(nullable = false)
    private String checkName;

    @Column(nullable = false)
    private int recordsChecked;

    @Column(nullable = false)
    private int recordsPassed;

    @Column(nullable = false)
    private int recordsFailed;

    @Column(nullable = false)
    private boolean passed;
}
