package com.taxgap.taxgapdetection.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taxgap.taxgapdetection.entity.Transaction;
import com.taxgap.taxgapdetection.repository.TransactionRepository;
import com.taxgap.taxgapdetection.service.RuleEngineService;
import com.taxgap.taxgapdetection.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final RuleEngineService ruleEngineService;

    public TransactionController(
            TransactionService transactionService,
            TransactionRepository transactionRepository,
            RuleEngineService ruleEngineService) {

        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
        this.ruleEngineService = ruleEngineService;
    }

    @PostMapping
    public ResponseEntity<Transaction> processTransaction(
            @RequestBody Transaction transaction) {

        Transaction savedTransaction =
                transactionService.processTransaction(transaction);

        if ("SUCCESS".equals(savedTransaction.getValidationStatus())) {
            ruleEngineService.executeRules(savedTransaction);
        }

        return ResponseEntity.ok(savedTransaction);
    }
    @PostMapping("/batch")
    public ResponseEntity<List<Transaction>> processBatchTransactions(
            @RequestBody List<Transaction> transactions) {

        List<Transaction> savedTransactions = transactions.stream()
                .map(transaction -> {

                    Transaction savedTransaction =
                            transactionService.processTransaction(transaction);

                    if ("SUCCESS".equals(savedTransaction.getValidationStatus())) {
                        ruleEngineService.executeRules(savedTransaction);
                    }

                    return savedTransaction;
                })
                .toList();

        return ResponseEntity.ok(savedTransactions);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {

        return ResponseEntity.ok(transactionRepository.findAll());
    }
}