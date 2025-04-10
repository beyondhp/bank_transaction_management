package com.bank.transaction.model;

/**
 * Enum representing the possible states of a transaction in a banking system
 */
public enum TransactionStatus {
    /**
     * Transaction has just been initiated and created in the system
     */
    INITIATED,
    
    /**
     * Transaction has been created but not yet processed
     */
    PENDING,
    
    /**
     * Transaction is being processed by the banking system
     */
    PROCESSING,
    
    /**
     * Transaction has been completed successfully
     */
    COMPLETED,
    
    /**
     * Transaction failed during processing
     */
    FAILED,
    
    /**
     * Transaction was rejected due to policy or validation issues
     */
    REJECTED,
    
    /**
     * Transaction was cancelled by the user or system
     */
    CANCELLED
} 