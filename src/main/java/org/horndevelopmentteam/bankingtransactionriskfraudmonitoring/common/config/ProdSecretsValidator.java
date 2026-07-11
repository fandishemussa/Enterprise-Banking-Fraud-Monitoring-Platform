package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Fails the app fast on startup under the "prod" profile if any secret is still at its insecure
 * local-dev default. Real secret storage (Vault, AWS Secrets Manager, k8s Secrets, etc.) is an
 * infrastructure decision outside this codebase - see docs/operations.md - but this at least stops
 * a misconfigured deploy from silently running with a well-known JWT signing key or admin password.
 */
@Component
@Profile("prod")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ProdSecretsValidator implements ApplicationRunner {

    private static final String INSECURE_JWT_SECRET =
            "local-dev-insecure-jwt-signing-secret-change-this-in-production-0123456789";
    private static final String INSECURE_ADMIN_PASSWORD = "Admin@12345";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${admin.default-password}")
    private String defaultAdminPassword;

    @Override
    public void run(ApplicationArguments args) {
        StringBuilder problems = new StringBuilder();

        if (jwtSecret == null || jwtSecret.equals(INSECURE_JWT_SECRET) || jwtSecret.length() < 32) {
            problems.append("- JWT_SECRET is missing, still the local-dev default, or shorter than 32 characters.\n");
        }
        if (defaultAdminPassword == null || defaultAdminPassword.equals(INSECURE_ADMIN_PASSWORD)) {
            problems.append("- DEFAULT_ADMIN_PASSWORD is missing or still the well-known local-dev default.\n");
        }

        if (!problems.isEmpty()) {
            throw new IllegalStateException(
                    "Refusing to start with profile=prod using insecure default secrets:\n" + problems
                            + "Set real values via environment variables (or a secrets manager - see docs/operations.md) "
                            + "before deploying to any environment reachable outside your own machine.");
        }
    }
}
