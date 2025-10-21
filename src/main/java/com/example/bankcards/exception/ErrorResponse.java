package com.example.bankcards.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/**
 * Формат ответа с информацией об ошибке HTTP.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
public class ErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final List<ValidationError> validationErrors;
    private final Map<String, Object> details;

    private ErrorResponse(Instant timestamp,
                          int status,
                          String error,
                          String message,
                          String path,
                          List<ValidationError> validationErrors,
                          Map<String, Object> details) {
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.validationErrors = validationErrors == null || validationErrors.isEmpty()
                ? null
                : Collections.unmodifiableList(validationErrors);
        this.details = details == null || details.isEmpty()
                ? null
                : Map.copyOf(details);
    }

    public record ValidationError(String field, String message) {
    }
}