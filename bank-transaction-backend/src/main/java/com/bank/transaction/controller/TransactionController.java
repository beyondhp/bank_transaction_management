package com.bank.transaction.controller;

import com.bank.transaction.model.Transaction;
import com.bank.transaction.model.TransactionPage;
import com.bank.transaction.model.TransactionStatus;
import com.bank.transaction.model.TransactionType;
import com.bank.transaction.service.TransactionService;
import com.bank.transaction.validator.TransactionValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction Controller", description = "API for managing bank transactions")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionValidator transactionValidator;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(transactionValidator);
    }

    @PostMapping
    @Operation(summary = "Create a new transaction")
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction, BindingResult bindingResult) {
        logger.info("Creating transaction: {}", transaction);
        
        if (bindingResult.hasErrors()) {
            logger.error("Validation errors: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(null);
        }
        
        try {
            Transaction createdTransaction = transactionService.createTransaction(transaction);
            return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create transaction: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a transaction by ID")
    public ResponseEntity<Transaction> getTransaction(
            @Parameter(description = "Transaction ID", required = true)
            @PathVariable Long id) {
        logger.info("Fetching transaction with ID: {}", id);
        
        Transaction transaction = transactionService.getTransaction(id);
        if (transaction == null) {
            logger.warn("Transaction not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(transaction);
    }

    @GetMapping
    @Operation(summary = "Get all transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        logger.info("Fetching all transactions");
        
        List<Transaction> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/paged")
    @Operation(summary = "Get transactions with pagination and filtering")
    public ResponseEntity<TransactionPage> getTransactionsPaged(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filter by transaction type")
            @RequestParam(required = false) TransactionType type,
            @Parameter(description = "Filter by transaction status")
            @RequestParam(required = false) TransactionStatus status,
            @Parameter(description = "Search by transaction ID")
            @RequestParam(required = false) String search) {
        
        logger.info("Fetching transactions page: {}, size: {}, type: {}, status: {}, search: {}", 
                page, size, type, status, search);
        
        List<Transaction> transactions = transactionService.getTransactionsPaged(page, size, type, status, search);
        long totalCount = transactionService.getTransactionCount(type, status, search);
        
        TransactionPage transactionPage = new TransactionPage(transactions, page, size, totalCount);
        return ResponseEntity.ok(transactionPage);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing transaction")
    public ResponseEntity<Transaction> updateTransaction(
            @Parameter(description = "Transaction ID", required = true)
            @PathVariable Long id,
            @RequestBody Transaction transaction) {
        
        logger.info("Updating transaction with ID: {}", id);
        
        try {
            Transaction updatedTransaction = transactionService.updateTransaction(id, transaction);
            return ResponseEntity.ok(updatedTransaction);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update transaction: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction")
    public ResponseEntity<Void> deleteTransaction(
            @Parameter(description = "Transaction ID", required = true)
            @PathVariable Long id) {
        
        logger.info("Deleting transaction with ID: {}", id);
        
        try {
            transactionService.deleteTransaction(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.error("Failed to delete transaction: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
} 