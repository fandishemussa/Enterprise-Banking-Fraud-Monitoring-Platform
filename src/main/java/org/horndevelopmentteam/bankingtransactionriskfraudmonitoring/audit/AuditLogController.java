package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.PageSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAllAuditLogs(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Pageable pageable = PageSupport.of(page, size, "createdAt");
        Page<AuditLogResponse> result = auditLogService.getAllAuditLogs(pageable);
        return PageSupport.withPageHeaders(
                ApiResponse.success(result.getContent()),
                result.getTotalElements(), result.getTotalPages(), result.getNumber(), result.getSize());
    }
}
