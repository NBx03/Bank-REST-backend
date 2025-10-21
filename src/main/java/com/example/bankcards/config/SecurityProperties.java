package com.example.bankcards.config;

import java.time.Duration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security")
@Getter
public class SecurityProperties {

    private final Jwt jwt = new Jwt();

    @Getter
    @Setter
    public static class Jwt {
        private String issuer;
        private String secret;
        private final Token accessToken = new Token();
        private final Token refreshToken = new Token();
    }

    @Getter
    @Setter
    public static class Token {
        private Duration ttl;
    }
}