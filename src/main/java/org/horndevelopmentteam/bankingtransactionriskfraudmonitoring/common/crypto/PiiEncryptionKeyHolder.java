package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Shared symmetric key for the PII field converters. Blank PII_ENCRYPTION_KEY (the default) means
 * encryption is a no-op everywhere - the same "secure when configured, permissive for local dev"
 * pattern used for SECURITY_ENABLED and ML_SERVICE_API_KEY elsewhere in this codebase. Set a real
 * base64-encoded 256-bit key via PII_ENCRYPTION_KEY for any deployment that stores real customer
 * data (see docs/operations.md).
 */
@Component
public class PiiEncryptionKeyHolder {

    private final byte[] rawKey;

    public PiiEncryptionKeyHolder(@Value("${pii.encryption.key:}") String base64Key) {
        this.rawKey = base64Key == null || base64Key.isBlank() ? null : Base64.getDecoder().decode(base64Key);
    }

    public boolean isEnabled() {
        return rawKey != null;
    }

    public SecretKeySpec aesKey() {
        return new SecretKeySpec(rawKey, "AES");
    }

    public byte[] rawKeyBytes() {
        return rawKey;
    }
}
