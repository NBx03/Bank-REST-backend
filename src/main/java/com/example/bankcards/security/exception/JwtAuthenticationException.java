package com.example.bankcards.security.exception;

import lombok.experimental.StandardException;
import org.springframework.security.core.AuthenticationException;

/**
 * Исключение, выбрасываемое при ошибках аутентификации по JWT.
 */
@StandardException
public class JwtAuthenticationException extends AuthenticationException {
}