package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dead-letter")
@RequiredArgsConstructor
public class DeadLetterController {

    private final DeadLetterService deadLetterService;

    @GetMapping("/transactions")
    public ApiResponse<List<DeadLetterResponse>> getAll() {
        return ApiResponse.success(deadLetterService.getAll());
    }

    @PostMapping("/transactions/{id}/retry")
    public ApiResponse<DeadLetterResponse> retry(@PathVariable("id") String eventId) {
        return ApiResponse.success("Retry attempted", deadLetterService.retry(eventId));
    }

    @PatchMapping("/transactions/{id}/ignore")
    public ApiResponse<DeadLetterResponse> ignore(@PathVariable("id") String eventId) {
        return ApiResponse.success("Entry ignored", deadLetterService.ignore(eventId));
    }
}
