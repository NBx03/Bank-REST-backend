package com.example.bankcards;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.bankcards.config.properties.EncryptionProperties;
import com.example.bankcards.config.properties.SecurityProperties;
import com.example.bankcards.testutil.YamlPropertySourceFactory;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(BankRestApplicationTest.PropertiesOnlyConfig.class)
class BankRestApplicationTest {

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private EncryptionProperties encryptionProperties;

    @Test
    void bindsSecurityPropertiesFromApplicationYaml() {
        assertThat(securityProperties.getJwt().getIssuer()).isEqualTo("bank-rest");
        assertThat(securityProperties.getJwt().getAccessToken().getTtl()).isEqualTo(Duration.ofMinutes(15));
        assertThat(securityProperties.getJwt().getRefreshToken().getTtl()).isEqualTo(Duration.ofDays(7));
        assertThat(securityProperties.getJwt().getSecret()).isEqualTo("default-bank-rest-jwt-secret-change-me-now-please!");
    }

    @Test
    void bindsEncryptionPropertiesFromApplicationYaml() {
        assertThat(encryptionProperties.getSecretKey()).isEqualTo("change-me-too");
    }

    @EnableConfigurationProperties({SecurityProperties.class, EncryptionProperties.class})
    @PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
    static class PropertiesOnlyConfig {
    }
}