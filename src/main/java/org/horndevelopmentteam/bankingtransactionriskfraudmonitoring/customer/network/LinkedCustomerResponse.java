package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.network;

import java.util.List;

public record LinkedCustomerResponse(
        String customerId,
        String fullName,
        List<String> sharedDeviceIds,
        List<String> sharedIpAddresses
) {
}
