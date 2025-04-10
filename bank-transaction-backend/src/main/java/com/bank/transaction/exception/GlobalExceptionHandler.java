package com.bank.transaction.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application
 * Standardizes error responses across the API
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles IllegalArgumentException which is typically thrown
     * when a transaction can't be found or other business logic validations fail
     * 
     * @param e The exception
     * @return ResponseEntity with standardized error structure
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Request",
            e.getMessage()
        );
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles validation errors from @Valid annotations
     * Returns a map of field names and their validation error messages
     * 
     * @param ex The validation exception
     * @return ResponseEntity with field error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = formatFieldName(error.getField());
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }
    
    /**
     * Formats field names to be more user-friendly
     * Converts camelCase to Title Case with spaces
     * 
     * @param fieldName The original field name
     * @return Formatted field name
     */
    private String formatFieldName(String fieldName) {
        // Convert camelCase to space-separated words
        String formatted = fieldName.replaceAll("([A-Z])", " $1");
        // Capitalize first letter
        formatted = formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
        return formatted;
    }
    
    /**
     * Handles any other exceptions that aren't specifically handled
     * 
     * @param e The exception
     * @return ResponseEntity with standardized error structure
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Server Error",
            "An unexpected error occurred. Please try again later."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

/**
 * Standard error response structure
 */
class ErrorResponse {
    private int status;
    private String error;
    private String message;
    
    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
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
} 