package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RiskScoreRepository extends JpaRepository<RiskScore, Long> {

    Optional<RiskScore> findByTransaction(BankingTransaction transaction);

    Optional<RiskScore> findByTransaction_TransactionId(String transactionId);

    List<RiskScore> findByTransactionIn(List<BankingTransaction> transactions);
}
