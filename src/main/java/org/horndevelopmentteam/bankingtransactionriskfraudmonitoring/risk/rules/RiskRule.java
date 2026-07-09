package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rules;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;

public interface RiskRule {

    RuleResult evaluate(BankingTransaction transaction);
}
