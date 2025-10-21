package com.example.bankcards.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Формат ответа с информацией об ошибке HTTP.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final List<ValidationError> validationErrors;

    private ErrorResponse(Builder builder) {
        this.timestamp = builder.timestamp;
        this.status = builder.status;
        this.error = builder.error;
        this.message = builder.message;
        this.path = builder.path;
        this.validationErrors = builder.validationErrors;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    public static Builder builder() {
        return new Builder();
    }

    public record ValidationError(String field, String message) {
    }

    public static class Builder {

        private Instant timestamp = Instant.now();
        private int status;
        private String error;
        private String message;
        private String path;
        private List<ValidationError> validationErrors;

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder validationErrors(List<ValidationError> validationErrors) {
            this.validationErrors = validationErrors == null || validationErrors.isEmpty()
                    ? null
                    : Collections.unmodifiableList(validationErrors);
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(this);
        }
    }
}