package com.taxgap.taxgapdetection.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxgap.taxgapdetection.entity.TaxRule;
import com.taxgap.taxgapdetection.entity.Transaction;
import com.taxgap.taxgapdetection.repository.TaxRuleRepository;
import com.taxgap.taxgapdetection.repository.TransactionRepository;

@Service
public class RuleEngineService {


private final TaxRuleRepository taxRuleRepository;
private final TransactionRepository transactionRepository;
private final ExceptionService exceptionService;
private final AuditLogService auditLogService;
private final ObjectMapper objectMapper = new ObjectMapper();

public RuleEngineService(
        TaxRuleRepository taxRuleRepository,
        TransactionRepository transactionRepository,
        ExceptionService exceptionService,
        AuditLogService auditLogService) {

    this.taxRuleRepository = taxRuleRepository;
    this.transactionRepository = transactionRepository;
    this.exceptionService = exceptionService;
    this.auditLogService = auditLogService;
}

public void executeRules(Transaction transaction) {

    List<TaxRule> activeRules = taxRuleRepository.findAll()
            .stream()
            .filter(TaxRule::isEnabled)
            .toList();

    for (TaxRule rule : activeRules) {

        if ("HIGH_VALUE".equals(rule.getRuleType())) {
            checkHighValueRule(transaction, rule);
        }

        if ("REFUND_VALIDATION".equals(rule.getRuleType())) {
            checkRefundRule(transaction, rule);
        }

        if ("GST_SLAB".equals(rule.getRuleType())) {
            checkGstSlabRule(transaction, rule);
        }

        auditLogService.log(
                "RULE_EXECUTION",
                transaction.getTransactionId(),
                "{\"rule\":\"" + rule.getRuleName() + "\"}"
        );
    }
}

private void checkHighValueRule(
        Transaction transaction,
        TaxRule rule) {

    try {

        JsonNode config =
                objectMapper.readTree(rule.getConfigurationJson());

        BigDecimal threshold;

        if (config.has("threshold")) {
            threshold = config.get("threshold").decimalValue();
        } else {
            threshold = new BigDecimal("100000");
        }

        if (transaction.getAmount().compareTo(threshold) > 0) {

            exceptionService.createException(
                    transaction.getTransactionId(),
                    transaction.getCustomerId(),
                    rule.getRuleName(),
                    "HIGH",
                    "High-value transaction exceeds threshold of "
                            + threshold
            );
        }

    } catch (Exception e) {

        auditLogService.log(
                "RULE_EXECUTION",
                transaction.getTransactionId(),
                "{\"error\":\"Invalid HIGH_VALUE rule configuration\"}"
        );
    }
}

private void checkRefundRule(
        Transaction transaction,
        TaxRule rule) {

    if (!"REFUND".equalsIgnoreCase(
            transaction.getTransactionType())) {
        return;
    }

    if (transaction.getOriginalTransactionId() == null
            || transaction.getOriginalTransactionId().isBlank()) {

        exceptionService.createException(
                transaction.getTransactionId(),
                transaction.getCustomerId(),
                rule.getRuleName(),
                "MEDIUM",
                "Original transaction ID is required for refund"
        );

        return;
    }

    Optional<Transaction> originalTransaction =
            transactionRepository.findByTransactionId(
                    transaction.getOriginalTransactionId()
            );

    if (originalTransaction.isEmpty()) {

        exceptionService.createException(
                transaction.getTransactionId(),
                transaction.getCustomerId(),
                rule.getRuleName(),
                "MEDIUM",
                "Original SALE transaction not found"
        );

        return;
    }

    Transaction originalSale = originalTransaction.get();

    if (!"SALE".equalsIgnoreCase(
            originalSale.getTransactionType())) {

        exceptionService.createException(
                transaction.getTransactionId(),
                transaction.getCustomerId(),
                rule.getRuleName(),
                "MEDIUM",
                "Original transaction is not a SALE transaction"
        );

        return;
    }

    if (transaction.getAmount()
            .compareTo(originalSale.getAmount()) > 0) {

        exceptionService.createException(
                transaction.getTransactionId(),
                transaction.getCustomerId(),
                rule.getRuleName(),
                "HIGH",
                "Refund amount exceeds original SALE amount"
        );
    }
}

private void checkGstSlabRule(
        Transaction transaction,
        TaxRule rule) {

    try {

        JsonNode config =
                objectMapper.readTree(rule.getConfigurationJson());

        BigDecimal slabThreshold;
        BigDecimal requiredTaxRate;

        if (config.has("slabThreshold")) {
            slabThreshold =
                    config.get("slabThreshold").decimalValue();
        } else {
            slabThreshold = new BigDecimal("50000");
        }

        if (config.has("requiredTaxRate")) {
            requiredTaxRate =
                    config.get("requiredTaxRate").decimalValue();
        } else {
            requiredTaxRate = new BigDecimal("0.18");
        }

        if (transaction.getAmount().compareTo(slabThreshold) > 0
                && transaction.getTaxRate()
                .compareTo(requiredTaxRate) < 0) {

            exceptionService.createException(
                    transaction.getTransactionId(),
                    transaction.getCustomerId(),
                    rule.getRuleName(),
                    "HIGH",
                    "GST slab violation detected. Required tax rate: "
                            + requiredTaxRate
            );
        }

    } catch (Exception e) {

        auditLogService.log(
                "RULE_EXECUTION",
                transaction.getTransactionId(),
                "{\"error\":\"Invalid GST_SLAB rule configuration\"}"
        );
    }
}


}
