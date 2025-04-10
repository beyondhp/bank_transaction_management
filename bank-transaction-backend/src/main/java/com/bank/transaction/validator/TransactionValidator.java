package com.bank.transaction.validator;

import com.bank.transaction.model.Transaction;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Custom validator for Transaction objects
 */
@Component
public class TransactionValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Transaction.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Transaction transaction = (Transaction) target;
        validateAccountsByTransactionType(transaction, errors);
    }

    /**
     * Validates source and destination accounts based on transaction type:
     * - DEPOSIT: Requires only destination account
     * - WITHDRAWAL: Requires only source account
     * - TRANSFER: Requires both source and destination accounts
     *
     * @param transaction The transaction to validate
     * @param errors The errors object to register validation errors
     */
    private void validateAccountsByTransactionType(Transaction transaction, Errors errors) {
        if (transaction.getType() == null) {
            return;
        }

        switch (transaction.getType()) {
            case DEPOSIT:
                // For deposits, destination account is required, source account should be null
                if (transaction.getDestinationAccount() == null || transaction.getDestinationAccount().isEmpty()) {
                    errors.rejectValue("destinationAccount", "destinationAccount.required", 
                            "Destination account is required for deposits");
                }
                // Source account should be null or empty for deposits - always set to null to be consistent
                if (transaction.getSourceAccount() != null && !transaction.getSourceAccount().isEmpty()) {
                    transaction.setSourceAccount(null); // Clear the source account for deposits
                }
                break;

            case WITHDRAWAL:
                // For withdrawals, source account is required, destination account should be null
                if (transaction.getSourceAccount() == null || transaction.getSourceAccount().isEmpty()) {
                    errors.rejectValue("sourceAccount", "sourceAccount.required", 
                            "Source account is required for withdrawals");
                }
                // Destination account should be null or empty for withdrawals - always set to null to be consistent
                if (transaction.getDestinationAccount() != null && !transaction.getDestinationAccount().isEmpty()) {
                    transaction.setDestinationAccount(null); // Clear the destination account for withdrawals
                }
                break;

            case TRANSFER:
                // For transfers, both source and destination accounts are required
                if (transaction.getSourceAccount() == null || transaction.getSourceAccount().isEmpty()) {
                    errors.rejectValue("sourceAccount", "sourceAccount.required", 
                            "Source account is required for transfers");
                }
                if (transaction.getDestinationAccount() == null || transaction.getDestinationAccount().isEmpty()) {
                    errors.rejectValue("destinationAccount", "destinationAccount.required", 
                            "Destination account is required for transfers");
                }
                break;
        }
    }
} 