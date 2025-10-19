package com.example.bankcards.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "transfer")
public class TransferProperties {

    private final Limit limit = new Limit();

    public Limit getLimit() {
        return limit;
    }

    public static class Limit {

        private BigDecimal daily = new BigDecimal("50000.00");

        public BigDecimal getDaily() {
            return daily;
        }

        public void setDaily(BigDecimal daily) {
            this.daily = daily;
        }
    }
}
