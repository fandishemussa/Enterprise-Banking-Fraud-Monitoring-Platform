package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.account;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ResourceNotFoundException;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerService customerService;
    private final IdSequenceService idSequenceService;
    private final AuditLogService auditLogService;

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        Customer customer = customerService.findByPublicIdOrThrow(request.customerId());
        LocalDateTime now = LocalDateTime.now();

        Account account = Account.builder()
                .accountId(idSequenceService.next("ACC"))
                .customer(customer)
                .accountType(request.accountType())
                .balance(request.balance())
                .currency(request.currency())
                .status(AccountStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
        Account saved = accountRepository.save(account);

        auditLogService.record(
                AuditEventType.ACCOUNT_CREATED,
                "Account",
                saved.getAccountId(),
                null,
                saved.getAccountType().name(),
                "Account " + saved.getAccountId() + " created for customer " + customer.getCustomerId()
        );

        return AccountResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(AccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountByPublicId(String accountId) {
        return AccountResponse.from(findByPublicIdOrThrow(accountId));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsForCustomer(String customerId) {
        Customer customer = customerService.findByPublicIdOrThrow(customerId);
        return accountRepository.findByCustomer(customer).stream()
                .map(AccountResponse::from)
                .toList();
    }

    public Account findByPublicIdOrThrow(String accountId) {
        return accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
    }
}
