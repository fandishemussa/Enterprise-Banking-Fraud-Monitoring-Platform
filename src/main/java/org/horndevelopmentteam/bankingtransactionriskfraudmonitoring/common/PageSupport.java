package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

/**
 * Shared pagination helper for the "hot" list endpoints (transactions, alerts, audit-logs,
 * risk-scores) that could otherwise return unbounded result sets. Keeps the existing
 * ApiResponse<List<T>> JSON shape (so the frontend doesn't need to change) and instead exposes
 * paging metadata as response headers - X-Total-Count / X-Total-Pages / X-Page / X-Page-Size -
 * which is the same pattern GitHub/Stripe use for list endpoints.
 */
public final class PageSupport {

    private static final int DEFAULT_SIZE = 50;
    private static final int MAX_SIZE = 200;

    private PageSupport() {
    }

    public static Pageable of(Integer page, Integer size, String sortField) {
        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = size == null || size <= 0 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        return PageRequest.of(safePage, safeSize, Sort.by(sortField).descending());
    }

    public static <T> ResponseEntity<ApiResponse<T>> withPageHeaders(
            ApiResponse<T> body, long totalElements, int totalPages, int page, int size) {
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(totalElements))
                .header("X-Total-Pages", String.valueOf(totalPages))
                .header("X-Page", String.valueOf(page))
                .header("X-Page-Size", String.valueOf(size))
                .body(body);
    }
}
