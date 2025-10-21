package com.example.bankcards;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.bankcards.config.properties.EncryptionProperties;
import com.example.bankcards.config.properties.SecurityProperties;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BankRestApplicationTests {

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private EncryptionProperties encryptionProperties;

    @Test
    void contextLoads() {
        assertThat(securityProperties.getJwt().getIssuer()).isEqualTo("bank-rest");
        assertThat(securityProperties.getJwt().getAccessToken().getTtl()).isEqualTo(Duration.ofMinutes(15));
        assertThat(securityProperties.getJwt().getRefreshToken().getTtl()).isEqualTo(Duration.ofDays(7));
        assertThat(encryptionProperties.getSecretKey()).isEqualTo("change-me-too");
    }
}