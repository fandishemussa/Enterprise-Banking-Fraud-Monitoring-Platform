package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rules;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums.RiskLevel;

public record RuleResult(String ruleName, String ruleCode, int points, String reason, RiskLevel severity) {

    /** Convenience constructor for rules that don't (yet) carry ruleCode/severity metadata. */
    public RuleResult(String ruleName, int points, String reason) {
        this(ruleName, null, points, reason, null);
    }

    public boolean triggered() {
        return points > 0;
    }
}
