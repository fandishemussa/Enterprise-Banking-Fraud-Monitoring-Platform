package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.dto.CustomerRequest;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.dto.CustomerResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.network.LinkedCustomerResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.network.LinkedCustomerService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.riskprofile.CustomerRiskProfileResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.riskprofile.CustomerRiskProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerRiskProfileService customerRiskProfileService;
    private final LinkedCustomerService linkedCustomerService;

    @PostMapping
    public ApiResponse<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        return ApiResponse.success("Customer created", customerService.createCustomer(request));
    }

    @GetMapping
    public ApiResponse<List<CustomerResponse>> getAllCustomers() {
        return ApiResponse.success(customerService.getAllCustomers());
    }

    @GetMapping("/{customerId}")
    public ApiResponse<CustomerResponse> getCustomer(@PathVariable String customerId) {
        return ApiResponse.success(customerService.getCustomerByPublicId(customerId));
    }

    @PutMapping("/{customerId}")
    public ApiResponse<CustomerResponse> updateCustomer(@PathVariable String customerId,
                                                         @Valid @RequestBody CustomerRequest request) {
        return ApiResponse.success("Customer updated", customerService.updateCustomer(customerId, request));
    }

    @GetMapping("/{customerId}/risk-profile")
    public ApiResponse<CustomerRiskProfileResponse> getRiskProfile(@PathVariable String customerId) {
        return ApiResponse.success(customerRiskProfileService.getRiskProfile(customerId));
    }

    @GetMapping("/{customerId}/linked-customers")
    public ApiResponse<List<LinkedCustomerResponse>> getLinkedCustomers(@PathVariable String customerId) {
        return ApiResponse.success(linkedCustomerService.getLinkedCustomers(customerId));
    }
}
