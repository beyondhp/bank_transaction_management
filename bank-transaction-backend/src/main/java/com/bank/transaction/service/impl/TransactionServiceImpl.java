package com.bank.transaction.service.impl;

import com.bank.transaction.model.Transaction;
import com.bank.transaction.model.TransactionType;
import com.bank.transaction.model.TransactionStatus;
import com.bank.transaction.service.TransactionService;
import com.bank.transaction.util.SnowflakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of the TransactionService interface
 * Provides in-memory storage and caching for bank transactions
 */
@Service
public class TransactionServiceImpl implements TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);
    
    // Thread-safe map to store transactions in memory
    private final Map<Long, Transaction> transactionStore = new ConcurrentHashMap<>();
    
    @Autowired
    private SnowflakeIdGenerator idGenerator;

    @Override
    @CachePut(value = "transactions", key = "#result.id")
    public Transaction createTransaction(Transaction transaction) {
        logger.info("Creating transaction in store: {}", transaction);
        transaction.setId(idGenerator.nextId());
        transactionStore.put(transaction.getId(), transaction);
        logger.info("Transaction created successfully. Store size: {}", transactionStore.size());
        return transaction;
    }

    /**
     * Retrieves a transaction by its ID
     * Results are cached to improve performance
     */
    @Override
    @Cacheable(value = "transactions", key = "#id", unless = "#result == null")
    public Transaction getTransaction(Long id) {
        logger.info("Retrieving transaction with ID: {}", id);
        Transaction transaction = transactionStore.get(id);
        if (transaction == null) {
            logger.warn("Transaction not found with ID: {}", id);
        }
        return transaction;
    }

    /**
     * Retrieves all transactions
     * Results are no longer cached for real-time data accuracy
     */
    @Override
    public List<Transaction> getAllTransactions() {
        logger.info("Retrieving all transactions. Store size: {}", transactionStore.size());
        List<Transaction> transactions = new ArrayList<>(transactionStore.values());
        logger.info("Returned {} transactions", transactions.size());
        return transactions;
    }
    
    /**
     * Retrieves transactions with pagination and filtering
     * Sorted by timestamp in descending order (newest first)
     * Results are no longer cached for real-time data accuracy
     */
    @Override
    public List<Transaction> getTransactionsPaged(int page, int size, 
                                                TransactionType type, 
                                                TransactionStatus status,
                                                String search) {
        logger.info("Retrieving filtered transactions. Page: {}, Size: {}, Type: {}, Status: {}, Search: {}", 
                    page, size, type, status, search);
        
        // Filter transactions based on criteria
        List<Transaction> filteredTransactions = transactionStore.values().stream()
                .filter(t -> type == null || t.getType() == type)
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> search == null || search.isEmpty() || 
                        (t.getId() != null && t.getId().toString().toLowerCase().contains(search.toLowerCase())) ||
                        (t.getId() != null && t.getId().toString().equals(search)))
                .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                .collect(Collectors.toList());
        
        logger.info("Filtered to {} transactions", filteredTransactions.size());
        
        int fromIndex = page * size;
        if (fromIndex >= filteredTransactions.size()) {
            if (filteredTransactions.size() > 0) {
                logger.warn("Page index {} exceeds the available data size {}", page, filteredTransactions.size());
            }
            return new ArrayList<>();
        }
        
        int toIndex = Math.min(fromIndex + size, filteredTransactions.size());
        List<Transaction> pagedTransactions = filteredTransactions.subList(fromIndex, toIndex);
        
        logger.info("Returned {} transactions for page {}", pagedTransactions.size(), page);
        return pagedTransactions;
    }
    
    /**
     * Gets the total count of transactions matching filters
     * Results are no longer cached for real-time data accuracy
     */
    @Override
    public long getTransactionCount(TransactionType type, TransactionStatus status, String search) {
        // Filter transactions based on criteria
        long count = transactionStore.values().stream()
                .filter(t -> type == null || t.getType() == type)
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> search == null || search.isEmpty() || 
                        (t.getId() != null && t.getId().toString().toLowerCase().contains(search.toLowerCase())) ||
                        (t.getId() != null && t.getId().toString().equals(search)))
                .count();
        
        logger.info("Filtered transaction count: {}", count);
        return count;
    }

    /**
     * Updates an existing transaction
     * Evicts only the cache for the specific transaction being updated
     * @throws IllegalArgumentException if the transaction doesn't exist
     */
    @Override
    @CachePut(value = "transactions", key = "#id")
    public Transaction updateTransaction(Long id, Transaction transaction) {
        logger.info("Updating transaction with ID: {}", id);
        if (!transactionStore.containsKey(id)) {
            logger.error("Transaction not found with ID: {}", id);
            throw new IllegalArgumentException("Transaction not found");
        }
        transaction.setId(id);
        transactionStore.put(id, transaction);
        logger.info("Transaction updated successfully");
        return transaction;
    }

    /**
     * Deletes a transaction
     * Evicts all cached transactions to ensure consistency
     * @throws IllegalArgumentException if the transaction doesn't exist
     */
    @Override
    @CacheEvict(value = "transactions", key = "#id")
    public void deleteTransaction(Long id) {
        logger.info("Deleting transaction with ID: {}", id);
        if (!transactionStore.containsKey(id)) {
            logger.error("Transaction not found with ID: {}", id);
            throw new IllegalArgumentException("Transaction not found");
        }
        transactionStore.remove(id);
        logger.info("Transaction deleted successfully. Store size: {}", transactionStore.size());
    }
} 