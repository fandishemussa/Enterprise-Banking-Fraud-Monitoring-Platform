package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.account;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/api/v1/accounts")
    public ApiResponse<AccountResponse> createAccount(@Valid @RequestBody AccountRequest request) {
        return ApiResponse.success("Account created", accountService.createAccount(request));
    }

    @GetMapping("/api/v1/accounts")
    public ApiResponse<List<AccountResponse>> getAllAccounts() {
        return ApiResponse.success(accountService.getAllAccounts());
    }

    @GetMapping("/api/v1/accounts/{accountId}")
    public ApiResponse<AccountResponse> getAccount(@PathVariable String accountId) {
        return ApiResponse.success(accountService.getAccountByPublicId(accountId));
    }

    @GetMapping("/api/v1/customers/{customerId}/accounts")
    public ApiResponse<List<AccountResponse>> getAccountsForCustomer(@PathVariable String customerId) {
        return ApiResponse.success(accountService.getAccountsForCustomer(customerId));
    }
}
