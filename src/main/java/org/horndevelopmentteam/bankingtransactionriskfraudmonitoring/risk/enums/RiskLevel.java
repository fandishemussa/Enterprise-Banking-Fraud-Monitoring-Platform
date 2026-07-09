package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums;

public enum RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public static RiskLevel fromScore(int score) {
        if (score >= 81) {
            return CRITICAL;
        }
        if (score >= 61) {
            return HIGH;
        }
        if (score >= 31) {
            return MEDIUM;
        }
        return LOW;
    }
}
