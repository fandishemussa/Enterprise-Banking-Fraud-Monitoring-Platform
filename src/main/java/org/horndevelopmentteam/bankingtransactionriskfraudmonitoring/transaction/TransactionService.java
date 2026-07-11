package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.account.Account;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.account.AccountService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.FraudAlertService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.BadRequestException;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ResourceNotFoundException;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.idempotency.IdempotencyService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.enums.CustomerStatus;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.RiskScore;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.RiskScoringService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final IdSequenceService idSequenceService;
    private final RiskScoringService riskScoringService;
    private final FraudAlertService fraudAlertService;
    private final AuditLogService auditLogService;
    private final IdempotencyService idempotencyService;

    private static final String IDEMPOTENCY_ENDPOINT = "transactions";

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        return createTransaction(request, null);
    }

    /** If idempotencyKey is non-blank: a retried request with the same key replays the original
     * transaction instead of creating a duplicate (see IdempotencyService for the claim protocol). */
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, String idempotencyKey) {
        boolean hasKey = idempotencyKey != null && !idempotencyKey.isBlank();
        if (hasKey) {
            Optional<String> existingTransactionId = idempotencyService.claim(idempotencyKey, IDEMPOTENCY_ENDPOINT);
            if (existingTransactionId.isPresent()) {
                return TransactionResponse.from(findByPublicIdOrThrow(existingTransactionId.get()));
            }
        }

        TransactionResponse response = doCreateTransaction(request);

        if (hasKey) {
            idempotencyService.complete(idempotencyKey, IDEMPOTENCY_ENDPOINT, response.transactionId());
        }
        return response;
    }

    private TransactionResponse doCreateTransaction(TransactionRequest request) {
        Account sourceAccount = accountService.findByPublicIdOrThrow(request.sourceAccountId());
        Account destinationAccount = request.destinationAccountId() != null
                ? accountService.findByPublicIdOrThrow(request.destinationAccountId())
                : null;
        Customer customer = sourceAccount.getCustomer();
        if (customer.getStatus() == CustomerStatus.LOCKED) {
            throw new BadRequestException("Customer " + customer.getCustomerId()
                    + " is locked pending fraud investigation and cannot transact");
        }

        BankingTransaction transaction = BankingTransaction.builder()
                .transactionId(idSequenceService.next("TXN"))
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .customer(customer)
                .amount(request.amount())
                .currency(request.currency())
                .transactionType(request.transactionType())
                .channel(request.channel())
                .merchantCategory(request.merchantCategory())
                .country(request.country())
                .deviceId(request.deviceId())
                .ipAddress(request.ipAddress())
                .status(TransactionStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();
        BankingTransaction saved = transactionRepository.save(transaction);

        auditLogService.record(
                AuditEventType.TRANSACTION_CREATED,
                "BankingTransaction",
                saved.getTransactionId(),
                null,
                saved.getAmount().toString(),
                "Transaction " + saved.getTransactionId() + " created for customer " + customer.getCustomerId()
        );

        RiskScore riskScore = riskScoringService.score(saved);
        fraudAlertService.createAlertIfWarranted(saved, riskScore);

        return TransactionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(TransactionResponse::from);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByPublicId(String transactionId) {
        return TransactionResponse.from(findByPublicIdOrThrow(transactionId));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsForCustomer(Customer customer) {
        return transactionRepository.findByCustomer(customer).stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsForAccount(Account account) {
        return transactionRepository.findBySourceAccount(account).stream()
                .map(TransactionResponse::from)
                .toList();
    }

    public BankingTransaction findByPublicIdOrThrow(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));
    }
}
