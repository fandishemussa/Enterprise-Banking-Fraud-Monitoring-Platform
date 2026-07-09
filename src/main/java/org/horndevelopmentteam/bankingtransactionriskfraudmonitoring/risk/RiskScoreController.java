package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.dto.RiskScoreResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RiskScoreController {

    private final RiskScoringService riskScoringService;

    @GetMapping("/api/v1/risk-scores")
    public ApiResponse<List<RiskScoreResponse>> getAllRiskScores() {
        return ApiResponse.success(riskScoringService.getAllRiskScores());
    }

    @GetMapping("/api/v1/transactions/{transactionId}/risk-score")
    public ApiResponse<RiskScoreResponse> getRiskScoreForTransaction(@PathVariable String transactionId) {
        return ApiResponse.success(riskScoringService.getRiskScoreForTransaction(transactionId));
    }
}
