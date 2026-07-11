package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AccessGuardTest {

    private final AccessGuard accessGuard = new AccessGuard();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deniesAnonymousRequestWhenSecurityEnabled() {
        ReflectionTestUtils.setField(accessGuard, "securityEnabled", true);
        SecurityContextHolder.clearContext();

        assertThat(accessGuard.allow("ADMIN")).isFalse();
    }

    @Test
    void allowsMatchingRoleWhenSecurityEnabled() {
        ReflectionTestUtils.setField(accessGuard, "securityEnabled", true);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("admin", null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        assertThat(accessGuard.allow("ADMIN")).isTrue();
        assertThat(accessGuard.allow("ANALYST", "ADMIN")).isTrue();
    }

    @Test
    void deniesNonMatchingRoleWhenSecurityEnabled() {
        ReflectionTestUtils.setField(accessGuard, "securityEnabled", true);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("viewer", null,
                        List.of(new SimpleGrantedAuthority("ROLE_VIEWER"))));

        assertThat(accessGuard.allow("ADMIN")).isFalse();
    }

    @Test
    void allowsEverythingWhenSecurityDisabledRegardlessOfAuthentication() {
        ReflectionTestUtils.setField(accessGuard, "securityEnabled", false);
        SecurityContextHolder.clearContext();

        assertThat(accessGuard.allow("ADMIN")).isTrue();
    }
}
