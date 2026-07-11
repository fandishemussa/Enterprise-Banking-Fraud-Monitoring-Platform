package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.PageSupport;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.dto.RiskScoreResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RiskScoreController {

    private final RiskScoringService riskScoringService;

    @GetMapping("/api/v1/risk-scores")
    public ResponseEntity<ApiResponse<List<RiskScoreResponse>>> getAllRiskScores(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Pageable pageable = PageSupport.of(page, size, "createdAt");
        Page<RiskScoreResponse> result = riskScoringService.getAllRiskScores(pageable);
        return PageSupport.withPageHeaders(
                ApiResponse.success(result.getContent()),
                result.getTotalElements(), result.getTotalPages(), result.getNumber(), result.getSize());
    }

    @GetMapping("/api/v1/transactions/{transactionId}/risk-score")
    public ApiResponse<RiskScoreResponse> getRiskScoreForTransaction(@PathVariable String transactionId) {
        return ApiResponse.success(riskScoringService.getRiskScoreForTransaction(transactionId));
    }
}
