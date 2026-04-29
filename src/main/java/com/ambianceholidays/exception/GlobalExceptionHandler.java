package com.ambianceholidays.exception;

import com.ambianceholidays.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException ex) {
        log.debug("Business exception: {} - {}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        String firstField = ex.getBindingResult().getFieldErrors().stream()
                .findFirst().map(FieldError::getField).orElse(null);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", errors, firstField));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("UNAUTHORIZED", "Authentication required"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("ACCESS_DENIED", "You do not have permission to perform this action"));
    }

    /**
     * DB constraint violations (column too long, NOT NULL violations, FK violations,
     * unique constraint hits, etc.). Surface a meaningful message to the API consumer
     * instead of swallowing it as a generic 500.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String root = rootMessage(ex);
        log.warn("Data integrity violation: {}", root);

        // Try to translate common Postgres / JDBC error patterns into something useful.
        String code = "DATA_INTEGRITY_ERROR";
        String message = "The data could not be saved because it violates a database constraint.";

        if (root != null) {
            String lower = root.toLowerCase();
            if (lower.contains("value too long for type")) {
                code = "VALUE_TOO_LONG";
                Matcher m = Pattern.compile("value too long for type (\\S+)").matcher(lower);
                String type = m.find() ? m.group(1) : "field";
                message = "One of the fields is too long for the database (" + type + "). "
                        + "If this is an image, please use the upload button to upload the file rather than pasting a long URL.";
            } else if (lower.contains("duplicate key") || lower.contains("unique constraint")) {
                code = "DUPLICATE_VALUE";
                message = "A record with the same unique value already exists.";
            } else if (lower.contains("violates foreign key constraint")) {
                code = "INVALID_REFERENCE";
                message = "One of the referenced records does not exist or has been removed.";
            } else if (lower.contains("violates not-null constraint")) {
                code = "MISSING_REQUIRED_FIELD";
                Matcher m = Pattern.compile("column \"([^\"]+)\"").matcher(root);
                String col = m.find() ? m.group(1) : "a required field";
                message = "Missing required field: " + col + ".";
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(code, message));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleMaxUpload(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.error("FILE_TOO_LARGE", "The uploaded file is too large."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("MALFORMED_REQUEST", "Request body is malformed or missing."));
    }

    @ExceptionHandler({ MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class })
    public ResponseEntity<ApiResponse<?>> handleBadParam(Exception ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_PARAMETER", "A request parameter is missing or has an invalid type."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred"));
    }

    /** Walk the cause chain so we surface the most informative message (Postgres' rather than Hibernate's wrapper). */
    private static String rootMessage(Throwable ex) {
        Throwable t = ex;
        String last = null;
        while (t != null) {
            if (t.getMessage() != null) last = t.getMessage();
            if (t.getCause() == t) break;
            t = t.getCause();
        }
        return last;
    }
}
