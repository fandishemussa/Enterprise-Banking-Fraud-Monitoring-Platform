package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rules;

public record RuleResult(String ruleName, int points, String reason) {

    public boolean triggered() {
        return points > 0;
    }
}
