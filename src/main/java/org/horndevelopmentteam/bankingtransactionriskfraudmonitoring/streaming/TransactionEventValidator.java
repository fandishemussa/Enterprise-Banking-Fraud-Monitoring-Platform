package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.streaming;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class TransactionEventValidator {

    /** Returns an empty list if the event is valid, otherwise every validation failure found. */
    public List<String> validate(TransactionEvent event) {
        List<String> errors = new ArrayList<>();
        if (event == null) {
            errors.add("event is null");
            return errors;
        }
        if (isBlank(event.sourceAccountId())) errors.add("sourceAccountId is required");
        if (event.amount() == null || event.amount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("amount must be greater than 0");
        }
        if (isBlank(event.currency())) errors.add("currency is required");
        if (isBlank(event.transactionType())) errors.add("transactionType is required");
        if (isBlank(event.channel())) errors.add("channel is required");
        if (isBlank(event.country())) errors.add("country is required");
        return errors;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
