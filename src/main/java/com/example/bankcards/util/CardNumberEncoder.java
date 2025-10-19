package com.example.bankcards.util;

import com.example.bankcards.config.EncryptionProperties;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Утилита для шифрования и маскирования номеров карт.
 */
@Component
public class CardNumberEncoder {

    private final SecretKeySpec secretKeySpec;

    public CardNumberEncoder(EncryptionProperties properties) {
        Assert.hasText(properties.getSecretKey(), "Encryption secret key must not be empty");
        byte[] keyBytes = properties.getSecretKey().getBytes(StandardCharsets.UTF_8);
        int keyLength = resolveKeyLength(keyBytes.length);
        byte[] resized = Arrays.copyOf(keyBytes, keyLength);
        this.secretKeySpec = new SecretKeySpec(resized, "AES");
    }

    public String encrypt(String plainCardNumber) {
        Assert.hasText(plainCardNumber, "Card number must not be empty");
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encrypted = cipher.doFinal(plainCardNumber.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to encrypt card number", e);
        }
    }

    public String decrypt(String encryptedCardNumber) {
        Assert.hasText(encryptedCardNumber, "Encrypted card number must not be empty");
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedCardNumber);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to decrypt card number", e);
        }
    }

    public String extractLastDigits(String cardNumber) {
        Assert.hasText(cardNumber, "Card number must not be empty");
        String normalized = cardNumber.replaceAll("\\s", "");
        return normalized.substring(Math.max(0, normalized.length() - 4));
    }

    public String mask(String cardNumber) {
        String lastDigits = extractLastDigits(cardNumber);
        return "**** **** **** " + lastDigits;
    }

    private int resolveKeyLength(int currentLength) {
        if (currentLength == 16 || currentLength == 24 || currentLength == 32) {
            return currentLength;
        }
        if (currentLength < 16) {
            return 16;
        }
        if (currentLength < 24) {
            return 24;
        }
        return 32;
    }
}