package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvestigationCaseRepository extends JpaRepository<InvestigationCase, Long> {

    Optional<InvestigationCase> findByCaseId(String caseId);

    long countByCustomerAndDecision(Customer customer, CaseDecision decision);
}
