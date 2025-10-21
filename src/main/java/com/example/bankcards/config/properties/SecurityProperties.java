package com.example.bankcards.config.properties;

import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "security")
@Validated
@Getter
public class SecurityProperties {

    @Valid
    @NotNull
    private final Jwt jwt = new Jwt();

    @Validated
    @Getter
    @Setter
    public static class Jwt {

        @NotBlank
        private String issuer;

        @NotBlank
        private String secret;

        @NotBlank
        private String algorithm = SignatureAlgorithm.HS256.getValue();

        @Valid
        @NotNull
        private final Token accessToken = new Token();

        @Valid
        @NotNull
        private final Token refreshToken = new Token();

        @Getter
        @Setter
        public static class Token {

            @NotNull
            @DurationMin(seconds = 1)
            private Duration ttl;

            @Setter(AccessLevel.NONE)
            private List<String> audience = List.of();
        }
    }
}