package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.riskprofile;

import java.time.LocalDateTime;
import java.util.List;

public record CustomerBehaviorSummary(
        List<String> devicesUsed,
        List<String> countriesUsed,
        List<String> highRiskMerchantCategoriesUsed,
        String mostUsedChannel,
        String mostUsedCountry,
        LocalDateTime lastTransactionAt,
        LocalDateTime lastAlertAt
) {
}
