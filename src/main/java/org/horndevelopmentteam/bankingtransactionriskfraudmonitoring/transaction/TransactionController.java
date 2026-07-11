package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.account.Account;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.account.AccountService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.PageSupport;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final CustomerService customerService;
    private final AccountService accountService;

    @PostMapping("/api/v1/transactions")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'TESTER')")
    public ApiResponse<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ApiResponse.success("Transaction created", transactionService.createTransaction(request, idempotencyKey));
    }

    @GetMapping("/api/v1/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactions(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Pageable pageable = PageSupport.of(page, size, "createdAt");
        Page<TransactionResponse> result = transactionService.getAllTransactions(pageable);
        return PageSupport.withPageHeaders(
                ApiResponse.success(result.getContent()),
                result.getTotalElements(), result.getTotalPages(), result.getNumber(), result.getSize());
    }

    @GetMapping("/api/v1/transactions/{transactionId}")
    public ApiResponse<TransactionResponse> getTransaction(@PathVariable String transactionId) {
        return ApiResponse.success(transactionService.getTransactionByPublicId(transactionId));
    }

    @GetMapping("/api/v1/customers/{customerId}/transactions")
    public ApiResponse<List<TransactionResponse>> getTransactionsForCustomer(@PathVariable String customerId) {
        Customer customer = customerService.findByPublicIdOrThrow(customerId);
        return ApiResponse.success(transactionService.getTransactionsForCustomer(customer));
    }

    @GetMapping("/api/v1/accounts/{accountId}/transactions")
    public ApiResponse<List<TransactionResponse>> getTransactionsForAccount(@PathVariable String accountId) {
        Account account = accountService.findByPublicIdOrThrow(accountId);
        return ApiResponse.success(transactionService.getTransactionsForAccount(account));
    }
}
