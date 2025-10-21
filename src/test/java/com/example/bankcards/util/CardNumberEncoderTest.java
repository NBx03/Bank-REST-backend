package com.example.bankcards.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.bankcards.config.properties.EncryptionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CardNumberEncoderTest {

    private CardNumberEncoder encoder;

    @BeforeEach
    void setUp() {
        EncryptionProperties properties = new EncryptionProperties();
        properties.setSecretKey("super-secret-key-123");
        encoder = new CardNumberEncoder(properties);
        encoder.init();
    }

    @Test
    void encryptAndDecrypt_shouldReturnOriginalValue() {
        String cardNumber = "4111222233334444";

        String encrypted = encoder.encrypt(cardNumber);
        String decrypted = encoder.decrypt(encrypted);

        assertThat(encrypted).isNotEqualTo(cardNumber);
        assertThat(decrypted).isEqualTo(cardNumber);
    }

    @Test
    void extractLastDigits_shouldIgnoreWhitespaces() {
        String result = encoder.extractLastDigits("4111 2222 3333 4444");

        assertThat(result).isEqualTo("4444");
    }

    @Test
    void mask_shouldReturnMaskedCardNumber() {
        String masked = encoder.mask("4111222233334444");

        assertThat(masked).isEqualTo("**** **** **** 4444");
    }

    @Test
    void encrypt_shouldThrowWhenCardNumberEmpty() {
        assertThatThrownBy(() -> encoder.encrypt(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void decrypt_shouldThrowWhenValueEmpty() {
        assertThatThrownBy(() -> encoder.decrypt(""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}