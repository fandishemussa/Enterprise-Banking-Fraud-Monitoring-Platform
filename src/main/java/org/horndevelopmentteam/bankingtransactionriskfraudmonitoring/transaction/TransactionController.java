package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.account.Account;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.account.AccountService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.CustomerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public ApiResponse<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        return ApiResponse.success("Transaction created", transactionService.createTransaction(request));
    }

    @GetMapping("/api/v1/transactions")
    public ApiResponse<List<TransactionResponse>> getAllTransactions() {
        return ApiResponse.success(transactionService.getAllTransactions());
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
