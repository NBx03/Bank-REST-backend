package com.example.bankcards.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "encryption")
@Getter
@Setter
public class EncryptionProperties {
    private String secretKey;
}