package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Referenced from @PreAuthorize as @access.allow("ADMIN", ...) instead of hasRole()/hasAnyRole()
 * directly, so that method security honors the same SECURITY_ENABLED=false escape hatch as the
 * URL-based rules in SecurityConfig - otherwise @PreAuthorize would keep blocking the anonymous
 * principal even with the toggle off, defeating the local-testing bypass.
 */
@Component("access")
public class AccessGuard {

    @Value("${security.enabled:true}")
    private boolean securityEnabled;

    public boolean allow(String... roles) {
        if (!securityEnabled) {
            return true;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        List<String> required = Arrays.stream(roles).map(role -> "ROLE_" + role).toList();
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> required.contains(authority.getAuthority()));
    }
}
