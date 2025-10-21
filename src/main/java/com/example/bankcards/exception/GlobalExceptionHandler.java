package com.example.bankcards.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Глобальный обработчик исключений REST-API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, ex);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request, ex);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request, ex);
    }

    @ExceptionHandler({
            UserInactiveException.class,
            CardInactiveException.class,
            TransferLimitExceededException.class,
            InsufficientFundsException.class,
            InvalidTransferRequestException.class,
            InvalidCardOperationException.class
    })
    public ResponseEntity<ErrorResponse> handleUnprocessableEntity(BankcardsException ex, HttpServletRequest request) {
        Map<String, Object> details = null;
        if (ex instanceof TransferLimitExceededException transferLimitExceededException) {
            details = Map.of("allowedLimit", transferLimitExceededException.getAllowedLimit());
        }
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request, ex, null, details);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponse.ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ErrorResponse.ValidationError(fieldError.getField(), resolveValidationMessage(fieldError)))
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, ex, errors, null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                   HttpServletRequest request) {
        List<ErrorResponse.ValidationError> errors = ex.getConstraintViolations().stream()
                .map(violation -> new ErrorResponse.ValidationError(violation.getPropertyPath().toString(), violation.getMessage()))
                .collect(Collectors.toList());
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, ex, errors, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        String message = String.format("Parameter '%s' has invalid value '%s'", ex.getName(), ex.getValue());
        return buildResponse(HttpStatus.BAD_REQUEST, message, request, ex);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, ex);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandler(NoHandlerFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, ex);
    }

    @ExceptionHandler(BankcardsException.class)
    public ResponseEntity<ErrorResponse> handleBankcardsException(BankcardsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandled(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred", request, ex);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, HttpServletRequest request, Exception ex) {
        return buildResponse(status, message, request, ex, null, null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status,
                                                        String message,
                                                        HttpServletRequest request,
                                                        Exception ex,
                                                        List<ErrorResponse.ValidationError> validationErrors,
                                                        Map<String, Object> details) {
        logHandledException(status, ex, request, validationErrors, details);
        ErrorResponse response = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .details(details)
                .build();
        return ResponseEntity.status(status).body(response);
    }

    private void logHandledException(HttpStatus status,
                                     Exception ex,
                                     HttpServletRequest request,
                                     List<ErrorResponse.ValidationError> validationErrors,
                                     Map<String, Object> details) {
        String uri = request.getRequestURI();
        if (status.is5xxServerError()) {
            log.error("Responding with {} {} for request {}", status.value(), status.getReasonPhrase(), uri, ex);
        } else if (ex != null) {
            log.warn("Responding with {} {} for request {} due to {}: {}",
                    status.value(),
                    status.getReasonPhrase(),
                    uri,
                    ex.getClass().getSimpleName(),
                    ex.getMessage());
        } else {
            log.info("Responding with {} {} for request {}", status.value(), status.getReasonPhrase(), uri);
        }

        if (validationErrors != null && !validationErrors.isEmpty()) {
            log.debug("Validation issues for request {}: {}", uri, validationErrors);
        }

        if (details != null && !details.isEmpty()) {
            log.debug("Error details for request {}: {}", uri, details);
        }

        if (ex != null && !status.is5xxServerError() && log.isDebugEnabled()) {
            log.debug("Stack trace for request {}", uri, ex);
        }
    }

    private String resolveValidationMessage(FieldError fieldError) {
        return fieldError.getDefaultMessage();
    }
}