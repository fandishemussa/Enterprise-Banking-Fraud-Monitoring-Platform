package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user.AppUser;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user.AppUserRepository;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user.UserStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Reads the Authorization header, and if it carries a valid JWT, populates the SecurityContext
 * with the username as principal and a single ROLE_<role> authority taken from the token claims.
 * Requests with no/invalid token simply proceed unauthenticated; permitAll endpoints still work,
 * everything else is rejected by the authorization rules in SecurityConfig.
 *
 * A DB lookup is done per authenticated request (not just signature/expiry verification) so that
 * logout, password change/reset, and disabling/locking a user take effect immediately - the token's
 * embedded tokenVersion must match the current value on the AppUser row, and the account must still
 * be ACTIVE. This trades a small amount of per-request latency for real revocation, since a purely
 * stateless JWT has no way to invalidate a token before its natural expiry otherwise.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            Claims claims = jwtService.parseClaims(token);

            if (claims != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                String username = claims.getSubject();
                String role = claims.get("role", String.class);
                Integer tokenVersion = claims.get("tokenVersion", Integer.class);

                AppUser user = appUserRepository.findByUsername(username).orElse(null);
                boolean valid = user != null
                        && user.getStatus() == UserStatus.ACTIVE
                        && tokenVersion != null
                        && tokenVersion == user.getTokenVersion();

                if (valid) {
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                    var authentication = UsernamePasswordAuthenticationToken.authenticated(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
