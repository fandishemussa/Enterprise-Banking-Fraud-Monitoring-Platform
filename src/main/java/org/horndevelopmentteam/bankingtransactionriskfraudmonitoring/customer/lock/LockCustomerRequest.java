package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.lock;

import jakarta.validation.constraints.NotBlank;

public record LockCustomerRequest(@NotBlank(message = "reason is required") String reason) {
}
