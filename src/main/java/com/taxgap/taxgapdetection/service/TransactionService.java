package com.taxgap.taxgapdetection.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.taxgap.taxgapdetection.entity.Transaction;
import com.taxgap.taxgapdetection.repository.TransactionRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TaxCalculationService taxCalculationService;
    private final ExceptionService exceptionService;
    private final AuditLogService auditLogService;

    public TransactionService(
            TransactionRepository transactionRepository,
            TaxCalculationService taxCalculationService,
            ExceptionService exceptionService,
            AuditLogService auditLogService) {

        this.transactionRepository = transactionRepository;
        this.taxCalculationService = taxCalculationService;
        this.exceptionService = exceptionService;
        this.auditLogService = auditLogService;
    }

    public Transaction processTransaction(Transaction transaction) {

        // Validation
        String validationError = validateTransaction(transaction);

        if (validationError != null) {

            transaction.setValidationStatus("FAILURE");
            transaction.setFailureReason(validationError);

            Transaction savedTransaction =
                    transactionRepository.save(transaction);

            auditLogService.log(
                    "INGESTION",
                    transaction.getTransactionId(),
                    "{\"status\":\"FAILURE\"}"
            );

            return savedTransaction;
        }

        // Validation success
        transaction.setValidationStatus("SUCCESS");

        // Tax calculation
        taxCalculationService.calculateTax(transaction);

        auditLogService.log(
                "TAX_COMPUTATION",
                transaction.getTransactionId(),
                "{\"status\":\"COMPLETED\"}"
        );

        // Create exception for non-compliance
        if (!"COMPLIANT".equals(transaction.getComplianceStatus())) {

            exceptionService.createException(
                    transaction.getTransactionId(),
                    transaction.getCustomerId(),
                    "TAX_COMPLIANCE_CHECK",
                    "HIGH",
                    "Transaction is " + transaction.getComplianceStatus()
            );
        }

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        auditLogService.log(
                "INGESTION",
                transaction.getTransactionId(),
                "{\"status\":\"SUCCESS\"}"
        );

        return savedTransaction;
    }
    private String validateTransaction(Transaction transaction) {

        if (transaction.getTransactionId() == null ||
                transaction.getTransactionId().isBlank()) {
            return "Transaction ID is required";
        }

        if (transaction.getDate() == null) {
            return "Date is required";
        }

        if (transaction.getCustomerId() == null ||
                transaction.getCustomerId().isBlank()) {
            return "Customer ID is required";
        }

        if (transaction.getAmount() == null ||
                transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return "Amount must be greater than 0";
        }

        if (transaction.getTaxRate() == null ||
                transaction.getTaxRate().compareTo(BigDecimal.ZERO) < 0) {
            return "Tax rate must be valid";
        }

        if (transaction.getReportedTax() == null) {
            return "Reported tax is required";
        }

        if (transaction.getTransactionType() == null ||
                transaction.getTransactionType().isBlank()) {
            return "Transaction type is required";
        }

        String transactionType =
                transaction.getTransactionType().toUpperCase();

        if (!transactionType.equals("SALE") &&
                !transactionType.equals("REFUND") &&
                !transactionType.equals("EXPENSE")) {

            return "Transaction type must be SALE, REFUND, or EXPENSE";
        }

        return null;
    }

    
}