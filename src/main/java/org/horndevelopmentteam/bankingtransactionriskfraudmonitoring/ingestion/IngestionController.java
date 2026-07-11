package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.BadRequestException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ingestion")
@RequiredArgsConstructor
public class IngestionController {

    private final CsvIngestionService csvIngestionService;

    @PostMapping("/upload")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'TESTER')")
    public ApiResponse<IngestionRunResponse> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }
        try {
            return ApiResponse.success("Ingestion run completed", csvIngestionService.ingest(file));
        } catch (IOException ex) {
            throw new BadRequestException("Failed to read uploaded file: " + ex.getMessage());
        }
    }

    @GetMapping("/runs")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'INVESTIGATOR', 'VIEWER', 'TESTER')")
    public ApiResponse<List<IngestionRunResponse>> getRuns() {
        return ApiResponse.success(csvIngestionService.getAllRuns());
    }

    @GetMapping("/rejected-records")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'INVESTIGATOR', 'VIEWER', 'TESTER')")
    public ApiResponse<List<RejectedTransactionResponse>> getRejectedRecords() {
        return ApiResponse.success(csvIngestionService.getRejectedRecords());
    }
}
