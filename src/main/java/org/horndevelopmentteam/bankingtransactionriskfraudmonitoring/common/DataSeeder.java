package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.account.AccountRequest;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.account.AccountService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.account.AccountType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.CustomerRepository;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.dto.CustomerRequest;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.dto.CustomerResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.CustomerService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionChannel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionRequest;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds a handful of customers/accounts/transactions on dev startup so the API
 * is immediately explorable. Skips seeding if data already exists (idempotent restarts).
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final AccountService accountService;
    private final TransactionService transactionService;

    @Override
    public void run(String... args) {
        if (customerRepository.count() > 0) {
            return;
        }

        CustomerResponse alice = customerService.createCustomer(
                new CustomerRequest("Alice Johnson", "alice.johnson@example.com", "+1-555-0101", "United States"));
        CustomerResponse bob = customerService.createCustomer(
                new CustomerRequest("Bob Martinez", "bob.martinez@example.com", "+1-555-0102", "Mexico"));

        var aliceAccount = accountService.createAccount(
                new AccountRequest(alice.customerId(), AccountType.CHECKING, BigDecimal.valueOf(15000), "USD"));
        var bobAccount = accountService.createAccount(
                new AccountRequest(bob.customerId(), AccountType.SAVINGS, BigDecimal.valueOf(8000), "USD"));

        // Ordinary, low-risk transaction
        transactionService.createTransaction(new TransactionRequest(
                aliceAccount.accountId(), null, BigDecimal.valueOf(120.50), "USD",
                TransactionType.PAYMENT, TransactionChannel.WEB, "GROCERY", "United States",
                "device-alice-01", "203.0.113.10"));

        // Large amount + new device + new country + high-risk merchant -> triggers HIGH/CRITICAL alert
        transactionService.createTransaction(new TransactionRequest(
                bobAccount.accountId(), null, BigDecimal.valueOf(12000), "USD",
                TransactionType.TRANSFER, TransactionChannel.MOBILE, "CRYPTO", "Nigeria",
                "device-bob-unknown", "198.51.100.23"));
    }
}
