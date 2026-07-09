package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvRowParserTest {

    private final CsvRowParser parser = new CsvRowParser();

    @Test
    void parsesAWellFormedRow() {
        String line = "TXN-1,CUS-1,ACC-1,,100.00,USD,PAYMENT,WEB,GROCERY,US,dev-1,127.0.0.1,SUCCESS,2026-01-01 10:00:00";

        CsvRowParser.ParsedRow row = parser.parse(line);

        assertThat(row.transactionId()).isEqualTo("TXN-1");
        assertThat(row.customerId()).isEqualTo("CUS-1");
        assertThat(row.destinationAccountId()).isNull();
        assertThat(row.amount()).isEqualTo("100.00");
        assertThat(row.status()).isEqualTo("SUCCESS");
    }

    @Test
    void blankFieldsBecomeNull() {
        String line = ",,,,,,,,,,,,,";

        CsvRowParser.ParsedRow row = parser.parse(line);

        assertThat(row.transactionId()).isNull();
        assertThat(row.amount()).isNull();
    }

    @Test
    void wrongColumnCountThrows() {
        String line = "too,few,columns";

        assertThatThrownBy(() -> parser.parse(line))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("14");
    }
}
