package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.testtransaction;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test-transactions")
@RequiredArgsConstructor
public class TestTransactionController {

    private final TestTransactionService testTransactionService;

    @PostMapping
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'TESTER')")
    public ApiResponse<TestTransactionResponse> submit(@Valid @RequestBody TestTransactionRequest request) {
        return ApiResponse.success("Test transaction processed successfully", testTransactionService.submit(request));
    }
}
