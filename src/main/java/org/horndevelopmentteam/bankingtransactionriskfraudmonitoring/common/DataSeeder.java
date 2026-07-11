package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.account.AccountRequest;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.account.AccountService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.account.AccountType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.FraudAlertService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.CustomerRepository;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.dto.CustomerRequest;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.dto.CustomerResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.CustomerService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionChannel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionRequest;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds a richer set of customers/accounts/transactions on dev startup, deliberately producing
 * several HIGH/CRITICAL alerts and assigning them across the demo users from DemoUserSeeder (which
 * runs first, see @Order) - so the assignment-based visibility rules (only ADMIN/VIEWER see every
 * alert/case; ANALYST/INVESTIGATOR/TESTER only see what's assigned to them) have real data to
 * exercise immediately after a fresh startup, without any manual setup. Skips seeding entirely if
 * data already exists (idempotent restarts).
 */
@Component
@Profile("dev")
@Order(2)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final FraudAlertService fraudAlertService;

    @Override
    public void run(String... args) {
        if (customerRepository.count() > 0) {
            return;
        }

        CustomerResponse alice = customerService.createCustomer(
                new CustomerRequest("Alice Johnson", "alice.johnson@example.com", "+1-555-0101", "United States"));
        CustomerResponse bob = customerService.createCustomer(
                new CustomerRequest("Bob Martinez", "bob.martinez@example.com", "+1-555-0102", "Mexico"));
        CustomerResponse carol = customerService.createCustomer(
                new CustomerRequest("Carol Chen", "carol.chen@example.com", "+86-138-0000-1234", "China"));
        CustomerResponse david = customerService.createCustomer(
                new CustomerRequest("David Okafor", "david.okafor@example.com", "+234-802-000-1122", "Nigeria"));

        var aliceAccount = accountService.createAccount(
                new AccountRequest(alice.customerId(), AccountType.CHECKING, BigDecimal.valueOf(15000), "USD"));
        var bobAccount = accountService.createAccount(
                new AccountRequest(bob.customerId(), AccountType.SAVINGS, BigDecimal.valueOf(8000), "USD"));
        var carolAccount = accountService.createAccount(
                new AccountRequest(carol.customerId(), AccountType.CHECKING, BigDecimal.valueOf(22000), "USD"));
        var davidAccount = accountService.createAccount(
                new AccountRequest(david.customerId(), AccountType.SAVINGS, BigDecimal.valueOf(5000), "USD"));

        // Ordinary, low-risk transaction - no alert
        transactionService.createTransaction(new TransactionRequest(
                aliceAccount.accountId(), null, BigDecimal.valueOf(120.50), "USD",
                TransactionType.PAYMENT, TransactionChannel.WEB, "GROCERY", "United States",
                "device-alice-01", "203.0.113.10"));

        // Large amount + new device + new country + high-risk merchant -> CRITICAL alert
        TransactionResponse bobTxn = transactionService.createTransaction(new TransactionRequest(
                bobAccount.accountId(), null, BigDecimal.valueOf(12000), "USD",
                TransactionType.TRANSFER, TransactionChannel.MOBILE, "CRYPTO", "Nigeria",
                "device-bob-unknown", "198.51.100.23"));

        // Large amount + new device + new country -> HIGH/CRITICAL alert
        TransactionResponse carolTxn = transactionService.createTransaction(new TransactionRequest(
                carolAccount.accountId(), null, BigDecimal.valueOf(9500), "USD",
                TransactionType.TRANSFER, TransactionChannel.MOBILE, "GAMBLING", "Russia",
                "device-carol-unknown", "203.0.113.77"));

        // Unusual hour + new device -> HIGH alert (timestamp-dependent rule, may or may not trigger,
        // that's fine either way - the amount/merchant/country combination alone is enough here)
        TransactionResponse davidTxn = transactionService.createTransaction(new TransactionRequest(
                davidAccount.accountId(), null, BigDecimal.valueOf(11000), "USD",
                TransactionType.WITHDRAWAL, TransactionChannel.ATM, "HIGH_RISK_TRANSFER", "Ukraine",
                "device-david-unknown", "198.51.100.45"));

        // Assign the generated alerts across demo users so the ANALYST/INVESTIGATOR-scoped
        // "only see what's assigned to me" views have something to show right after startup.
        // Best-effort: DemoUserSeeder runs first (see @Order), but if it's ever skipped or the
        // roles change, a missing assignee should never fail the whole seeding run.
        assignAlertIfPresent(bobTxn.transactionId(), "analyst1");
        assignAlertIfPresent(carolTxn.transactionId(), "investigator1");
        assignAlertIfPresent(davidTxn.transactionId(), "analyst2");

        log.info("Seeded {} demo customers with sample transactions and alerts.", 4);
    }

    private void assignAlertIfPresent(String transactionId, String assignee) {
        fraudAlertService.findByTransactionId(transactionId)
                .ifPresent(alert -> fraudAlertService.assign(alert.alertId(), assignee));
    }
}
