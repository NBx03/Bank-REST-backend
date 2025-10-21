package com.example.bankcards.config.properties;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "transfer")
@Getter
public class TransferProperties {

    private final Limit limit = new Limit();

    @Getter
    @Setter
    public static class Limit {
        private BigDecimal daily = new BigDecimal("50000.00");
    }
}
