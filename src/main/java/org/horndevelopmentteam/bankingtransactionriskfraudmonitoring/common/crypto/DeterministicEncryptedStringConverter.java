package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/**
 * Deterministic AES-GCM encryption at rest: the IV is derived from HMAC-SHA256(key, plaintext)
 * instead of being random, so the same plaintext always produces the same stored value. This is
 * what lets an encrypted column keep working as an exact-match lookup key
 * (AppUserRepository.findByEmail, CustomerRepository.existsByEmail,
 * TransactionRepository.existsByCustomerAndDeviceIdAndIdNot) and keep a DB-level UNIQUE constraint
 * meaningful (same plaintext -> same ciphertext -> constraint still catches real duplicates). The
 * trade-off, and the reason this is a distinct converter from EncryptedStringConverter rather than
 * the default: it leaks equality (an attacker with DB access can tell two rows share a value, just
 * not what the value is) - acceptable for fields that must remain searchable, wrong for anything
 * that doesn't need to be. No-op if PII_ENCRYPTION_KEY isn't configured (see
 * PiiEncryptionKeyHolder).
 */
@Converter
@Component
@RequiredArgsConstructor
public class DeterministicEncryptedStringConverter implements AttributeConverter<String, String> {

    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final PiiEncryptionKeyHolder keyHolder;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        if (!keyHolder.isEnabled()) return attribute;

        try {
            byte[] iv = deriveIv(attribute);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keyHolder.aesKey(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to encrypt field", ex);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        if (!keyHolder.isEnabled()) return dbData;

        try {
            byte[] combined = Base64.getDecoder().decode(dbData);
            byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH_BYTES);
            byte[] ciphertext = Arrays.copyOfRange(combined, IV_LENGTH_BYTES, combined.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keyHolder.aesKey(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to decrypt field", ex);
        }
    }

    private byte[] deriveIv(String attribute) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(keyHolder.rawKeyBytes(), "HmacSHA256"));
        byte[] digest = mac.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
        return Arrays.copyOf(digest, IV_LENGTH_BYTES);
    }
}
