package com.example.bankcards.controller.support;

import com.example.bankcards.exception.InvalidOperatorContextException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Компонент извлечения ID оператора из HTTP-заголовков.
 */
@Component
public class OperatorContextResolver {

    public static final String OPERATOR_HEADER = "X-Operator-Id";

    public Long resolveOperatorId(HttpServletRequest request) {
        String headerValue = request.getHeader(OPERATOR_HEADER);
        if (!StringUtils.hasText(headerValue)) {
            throw new InvalidOperatorContextException("X-Operator-Id header is required");
        }
        try {
            return Long.parseLong(headerValue);
        } catch (NumberFormatException ex) {
            throw new InvalidOperatorContextException("X-Operator-Id header must contain a valid numeric value", ex);
        }
    }
}