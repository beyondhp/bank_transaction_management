package com.bank.transaction.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction entity representing a bank transaction
 * Contains details such as account information, transaction status
 * 
 * Note: ID is a 64-bit long integer generated using Snowflake algorithm
 */
@Data
public class Transaction {
    private Long id;
    
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Transaction type is required")
    private TransactionType type;
    
    private LocalDateTime timestamp;
    
    // Source and destination account information
    @Pattern(regexp = "^[A-Z0-9]{8,20}$", message = "Invalid account format")
    private String sourceAccount;
    
    @Pattern(regexp = "^[A-Z0-9]{8,20}$", message = "Invalid account format")
    private String destinationAccount;
    
    // Transaction status
    @NotNull(message = "Status is required")
    private TransactionStatus status = TransactionStatus.INITIATED;
    
    // Bank-generated reference
    private String bankReference;
    
    // Processing date (when the transaction was processed by the bank)
    private LocalDateTime processingDate;
    
    public Transaction() {
        this.timestamp = LocalDateTime.now();
    }
} 