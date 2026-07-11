package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.notification;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** Defaults to the last 24h if the client has no prior poll timestamp (first load). */
    @GetMapping("/api/v1/notifications")
    public ApiResponse<List<NotificationItem>> getNotifications(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since,
            Authentication authentication) {
        LocalDateTime effectiveSince = since != null ? since : LocalDateTime.now().minusHours(24);
        return ApiResponse.success(
                notificationService.getNotifications(authentication.getName(), roleOf(authentication), effectiveSince));
    }

    private String roleOf(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
                .orElse(null);
    }
}
