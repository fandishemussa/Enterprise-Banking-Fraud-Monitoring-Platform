package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.lock;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CustomerLockController {

    private final CustomerLockService customerLockService;

    /** ADMIN locks immediately; INVESTIGATOR only creates a PENDING request (see CustomerLockService). */
    @PostMapping("/api/v1/customers/{customerId}/lock")
    @PreAuthorize("@access.allow('ADMIN', 'INVESTIGATOR')")
    public ApiResponse<CustomerLockRequestResponse> lock(@PathVariable String customerId,
                                                          @Valid @RequestBody LockCustomerRequest request,
                                                          Authentication authentication) {
        CustomerLockRequestResponse response = customerLockService.lock(
                customerId, request.reason(), authentication.getName(), roleOf(authentication));
        String message = response.status() == CustomerLockRequestStatus.APPROVED
                ? "Customer locked"
                : "Lock request submitted for admin approval";
        return ApiResponse.success(message, response);
    }

    @PatchMapping("/api/v1/customers/lock-requests/{lockRequestId}/approve")
    @PreAuthorize("@access.allow('ADMIN')")
    public ApiResponse<CustomerLockRequestResponse> approve(@PathVariable String lockRequestId,
                                                             @RequestBody(required = false) ReviewLockRequest request,
                                                             Authentication authentication) {
        String notes = request != null ? request.notes() : null;
        return ApiResponse.success("Lock request approved",
                customerLockService.approve(lockRequestId, authentication.getName(), notes));
    }

    @PatchMapping("/api/v1/customers/lock-requests/{lockRequestId}/reject")
    @PreAuthorize("@access.allow('ADMIN')")
    public ApiResponse<CustomerLockRequestResponse> reject(@PathVariable String lockRequestId,
                                                            @RequestBody(required = false) ReviewLockRequest request,
                                                            Authentication authentication) {
        String notes = request != null ? request.notes() : null;
        return ApiResponse.success("Lock request rejected",
                customerLockService.reject(lockRequestId, authentication.getName(), notes));
    }

    @PostMapping("/api/v1/customers/{customerId}/unlock")
    @PreAuthorize("@access.allow('ADMIN')")
    public ApiResponse<Void> unlock(@PathVariable String customerId, Authentication authentication) {
        customerLockService.unlock(customerId, authentication.getName());
        return ApiResponse.success("Customer unlocked", null);
    }

    @GetMapping("/api/v1/customers/{customerId}/lock-requests")
    @PreAuthorize("@access.allow('ADMIN', 'INVESTIGATOR')")
    public ApiResponse<List<CustomerLockRequestResponse>> getRequestsForCustomer(@PathVariable String customerId) {
        return ApiResponse.success(customerLockService.getRequestsForCustomer(customerId));
    }

    @GetMapping("/api/v1/customers/lock-requests/pending")
    @PreAuthorize("@access.allow('ADMIN', 'INVESTIGATOR')")
    public ApiResponse<List<CustomerLockRequestResponse>> getPendingRequests() {
        return ApiResponse.success(customerLockService.getPendingRequests());
    }

    private String roleOf(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
                .orElse(null);
    }
}
