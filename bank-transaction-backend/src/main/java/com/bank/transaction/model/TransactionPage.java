package com.bank.transaction.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A container for paged transaction data
 * Includes both the transactions and metadata about the pagination
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPage {
    private List<Transaction> content;     // List of transactions for the current page
    private int pageNumber;                // Current page number (0-based)
    private int pageSize;                  // Page size
    private long totalElements;            // Total number of transactions
    private long totalPages;               // Total number of pages
    
    /**
     * Constructor with 4 parameters that automatically calculates total pages
     * 
     * @param content List of transactions for the current page
     * @param pageNumber Current page number (0-based)
     * @param pageSize Page size
     * @param totalElements Total number of transactions
     */
    public TransactionPage(List<Transaction> content, int pageNumber, int pageSize, long totalElements) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = pageSize > 0 ? (long) Math.ceil((double) totalElements / pageSize) : 0;
    }
    
    // Convenience methods for pagination logic
    public boolean isFirst() {
        return pageNumber == 0;
    }
    
    public boolean isLast() {
        return pageNumber == totalPages - 1;
    }
    
    public boolean hasNext() {
        return pageNumber < totalPages - 1;
    }
    
    public boolean hasPrevious() {
        return pageNumber > 0;
    }
} 