package com.bank.transaction.service;

import com.bank.transaction.model.Transaction;
import com.bank.transaction.model.TransactionType;
import com.bank.transaction.model.TransactionStatus;

import java.util.List;

/**
 * Service interface for transaction management
 */
public interface TransactionService {
    
    /**
     * Creates a new transaction
     * 
     * @param transaction Transaction to create
     * @return Created transaction with ID
     */
    Transaction createTransaction(Transaction transaction);
    
    /**
     * Retrieves a transaction by ID
     * 
     * @param id Transaction ID
     * @return Transaction if found, null otherwise
     */
    Transaction getTransaction(Long id);
    
    /**
     * Retrieves all transactions
     * 
     * @return List of all transactions
     */
    List<Transaction> getAllTransactions();
    
    /**
     * Retrieves transactions with pagination and filtering
     * 
     * @param page Page number (0-based)
     * @param size Page size
     * @param type Transaction type filter (optional)
     * @param status Transaction status filter (optional)
     * @param search Search text for transaction ID (optional)
     * @return List of transactions for the requested page and filters
     */
    List<Transaction> getTransactionsPaged(int page, int size, 
                                          TransactionType type, 
                                          TransactionStatus status,
                                          String search);
    
    /**
     * Gets the total count of transactions matching filters
     * 
     * @param type Transaction type filter (optional)
     * @param status Transaction status filter (optional)
     * @param search Search text for transaction ID (optional)
     * @return Total number of transactions matching filters
     */
    long getTransactionCount(TransactionType type, TransactionStatus status, String search);
    
    /**
     * Updates an existing transaction
     * 
     * @param id Transaction ID
     * @param transaction Updated transaction data
     * @return Updated transaction
     * @throws IllegalArgumentException if transaction doesn't exist
     */
    Transaction updateTransaction(Long id, Transaction transaction);
    
    /**
     * Deletes a transaction
     * 
     * @param id Transaction ID
     * @throws IllegalArgumentException if transaction doesn't exist
     */
    void deleteTransaction(Long id);
} 