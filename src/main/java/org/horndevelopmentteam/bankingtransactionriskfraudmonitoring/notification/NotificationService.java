package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.notification;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.FraudAlertResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.FraudAlertService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla.AlertEscalation;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla.AlertEscalationRepository;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.lock.CustomerLockRequest;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.lock.CustomerLockRequestRepository;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.lock.CustomerLockRequestStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Read-only, on-the-fly notification feed - deliberately not a persisted "notifications" table with
 * read/unread state. Everything here is derived from data that already exists (alerts, escalations,
 * pending lock requests), filtered to what's new since the client's last poll and to what this
 * user's role can already see (reuses FraudAlertService#getAlertsVisibleTo, so a notification never
 * reveals an alert the Alerts page itself would hide from this user).
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int MAX_ITEMS = 30;

    private final FraudAlertService fraudAlertService;
    private final AlertEscalationRepository alertEscalationRepository;
    private final CustomerLockRequestRepository lockRequestRepository;

    @Transactional(readOnly = true)
    public List<NotificationItem> getNotifications(String username, String role, LocalDateTime since) {
        List<NotificationItem> items = new ArrayList<>();

        fraudAlertService.getAlertsVisibleTo(username, role, PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent().stream()
                .filter(alert -> alert.createdAt().isAfter(since))
                .map(this::toNewAlertItem)
                .forEach(items::add);

        alertEscalationRepository.findByEscalatedToAndCreatedAtAfter(username, since).stream()
                .map(this::toEscalationItem)
                .forEach(items::add);

        if ("ADMIN".equals(role)) {
            lockRequestRepository.findByStatusOrderByCreatedAtDesc(CustomerLockRequestStatus.PENDING).stream()
                    .filter(request -> request.getCreatedAt().isAfter(since))
                    .map(this::toLockRequestItem)
                    .forEach(items::add);
        }

        return items.stream()
                .sorted(Comparator.comparing(NotificationItem::createdAt).reversed())
                .limit(MAX_ITEMS)
                .toList();
    }

    private NotificationItem toNewAlertItem(FraudAlertResponse alert) {
        return new NotificationItem(
                "alert-" + alert.alertId(),
                NotificationType.NEW_ALERT,
                "New " + alert.priority() + " alert",
                "Alert " + alert.alertId() + " for customer " + alert.customerId(),
                alert.alertId(),
                alert.createdAt());
    }

    private NotificationItem toEscalationItem(AlertEscalation escalation) {
        return new NotificationItem(
                "escalation-" + escalation.getEscalationId(),
                NotificationType.ALERT_ESCALATED,
                "Alert escalated to you",
                "Alert " + escalation.getAlertId() + " escalated by " + escalation.getEscalatedFrom()
                        + (escalation.getReason() != null ? ": " + escalation.getReason() : ""),
                escalation.getAlertId(),
                escalation.getCreatedAt());
    }

    private NotificationItem toLockRequestItem(CustomerLockRequest request) {
        return new NotificationItem(
                "lock-" + request.getLockRequestId(),
                NotificationType.LOCK_REQUEST_PENDING,
                "Lock request pending approval",
                "Customer " + request.getCustomer().getCustomerId() + " lock requested by " + request.getRequestedBy(),
                request.getLockRequestId(),
                request.getCreatedAt());
    }
}
