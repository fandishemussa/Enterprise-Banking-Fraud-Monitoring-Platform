package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.network;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.CustomerService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Fraud-ring detection: finds other customers linked to this one via a shared device ID or IP
 * address across transactions - the same signal SharedDeviceOrIpRule uses at scoring time, exposed
 * here as a browsable list for an investigator building a case ("who else used this device?").
 */
@Service
@RequiredArgsConstructor
public class LinkedCustomerService {

    private final CustomerService customerService;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<LinkedCustomerResponse> getLinkedCustomers(String customerId) {
        Customer customer = customerService.findByPublicIdOrThrow(customerId);
        List<BankingTransaction> ownTransactions = transactionRepository.findByCustomer(customer);

        List<String> deviceIds = ownTransactions.stream()
                .map(BankingTransaction::getDeviceId).filter(Objects::nonNull).distinct().toList();
        List<String> ipAddresses = ownTransactions.stream()
                .map(BankingTransaction::getIpAddress).filter(Objects::nonNull).distinct().toList();

        Map<Customer, Accumulator> byCustomer = new LinkedHashMap<>();

        if (!deviceIds.isEmpty()) {
            for (BankingTransaction t : transactionRepository.findByDeviceIdIn(deviceIds)) {
                if (t.getCustomer().getId().equals(customer.getId())) continue;
                byCustomer.computeIfAbsent(t.getCustomer(), c -> new Accumulator()).sharedDeviceIds.add(t.getDeviceId());
            }
        }
        if (!ipAddresses.isEmpty()) {
            for (BankingTransaction t : transactionRepository.findByIpAddressIn(ipAddresses)) {
                if (t.getCustomer().getId().equals(customer.getId())) continue;
                byCustomer.computeIfAbsent(t.getCustomer(), c -> new Accumulator()).sharedIpAddresses.add(t.getIpAddress());
            }
        }

        return byCustomer.entrySet().stream()
                .map(entry -> new LinkedCustomerResponse(
                        entry.getKey().getCustomerId(),
                        entry.getKey().getFullName(),
                        entry.getValue().sharedDeviceIds.stream().distinct().toList(),
                        entry.getValue().sharedIpAddresses.stream().distinct().toList()))
                .toList();
    }

    private static class Accumulator {
        private final List<String> sharedDeviceIds = new ArrayList<>();
        private final List<String> sharedIpAddresses = new ArrayList<>();
    }
}
