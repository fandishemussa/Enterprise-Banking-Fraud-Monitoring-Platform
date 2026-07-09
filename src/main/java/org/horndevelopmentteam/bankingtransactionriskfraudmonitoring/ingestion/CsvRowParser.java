package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion;

import org.springframework.stereotype.Component;

/**
 * Parses one CSV data line into its raw string fields. Expected column order:
 * transactionId,customerId,sourceAccountId,destinationAccountId,amount,currency,
 * transactionType,channel,merchantCategory,country,deviceId,ipAddress,status,createdAt
 */
@Component
public class CsvRowParser {

    public static final int EXPECTED_COLUMN_COUNT = 14;

    public record ParsedRow(
            String transactionId,
            String customerId,
            String sourceAccountId,
            String destinationAccountId,
            String amount,
            String currency,
            String transactionType,
            String channel,
            String merchantCategory,
            String country,
            String deviceId,
            String ipAddress,
            String status,
            String createdAt
    ) {
    }

    public ParsedRow parse(String csvLine) {
        String[] columns = csvLine.split(",", -1);
        if (columns.length != EXPECTED_COLUMN_COUNT) {
            throw new IllegalArgumentException(
                    "Expected " + EXPECTED_COLUMN_COUNT + " columns but found " + columns.length);
        }
        for (int i = 0; i < columns.length; i++) {
            columns[i] = columns[i].trim();
        }
        return new ParsedRow(
                blankToNull(columns[0]), blankToNull(columns[1]), blankToNull(columns[2]), blankToNull(columns[3]),
                blankToNull(columns[4]), blankToNull(columns[5]), blankToNull(columns[6]), blankToNull(columns[7]),
                blankToNull(columns[8]), blankToNull(columns[9]), blankToNull(columns[10]), blankToNull(columns[11]),
                blankToNull(columns[12]), blankToNull(columns[13])
        );
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
