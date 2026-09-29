package com.taxgap.taxgapdetection.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.taxgap.taxgapdetection.entity.ExceptionRecord;
import com.taxgap.taxgapdetection.entity.Transaction;
import com.taxgap.taxgapdetection.repository.ExceptionRecordRepository;
import com.taxgap.taxgapdetection.repository.TransactionRepository;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final ExceptionRecordRepository exceptionRecordRepository;

    public ReportService(
            TransactionRepository transactionRepository,
            ExceptionRecordRepository exceptionRecordRepository) {

        this.transactionRepository = transactionRepository;
        this.exceptionRecordRepository = exceptionRecordRepository;
    }

    public Map<String, Object> getCustomerTaxSummary(String customerId) {

        List<Transaction> transactions = transactionRepository.findAll()
                .stream()
                .filter(t -> customerId.equals(t.getCustomerId()))
                .toList();

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalReportedTax = BigDecimal.ZERO;
        BigDecimal totalExpectedTax = BigDecimal.ZERO;
        BigDecimal totalTaxGap = BigDecimal.ZERO;

        int nonCompliantTransactions = 0;

        for (Transaction transaction : transactions) {

            if (transaction.getAmount() != null) {
                totalAmount = totalAmount.add(transaction.getAmount());
            }

            if (transaction.getReportedTax() != null) {
                totalReportedTax =
                        totalReportedTax.add(transaction.getReportedTax());
            }

            if (transaction.getExpectedTax() != null) {
                totalExpectedTax =
                        totalExpectedTax.add(transaction.getExpectedTax());
            }

            if (transaction.getTaxGap() != null) {
                totalTaxGap =
                        totalTaxGap.add(transaction.getTaxGap());
            }

            if (!"COMPLIANT".equals(transaction.getComplianceStatus())) {
                nonCompliantTransactions++;
            }
        }

        double complianceScore = 0;

        if (!transactions.isEmpty()) {
            complianceScore = 100 -
                    ((double) nonCompliantTransactions
                    / transactions.size() * 100);
        }

        Map<String, Object> report = new HashMap<>();

        report.put("customerId", customerId);
        report.put("totalTransactions", transactions.size());
        report.put("totalAmount", totalAmount);
        report.put("totalReportedTax", totalReportedTax);
        report.put("totalExpectedTax", totalExpectedTax);
        report.put("totalTaxGap", totalTaxGap);
        report.put("complianceScore", complianceScore);

        return report;
    }

    public Map<String, Object> getExceptionSummary() {

        List<ExceptionRecord> exceptions =
                exceptionRecordRepository.findAll();

        long high = exceptions.stream()
                .filter(e -> "HIGH".equals(e.getSeverity()))
                .count();

        long medium = exceptions.stream()
                .filter(e -> "MEDIUM".equals(e.getSeverity()))
                .count();

        long low = exceptions.stream()
                .filter(e -> "LOW".equals(e.getSeverity()))
                .count();

        Map<String, Object> report = new HashMap<>();

        report.put("totalExceptions", exceptions.size());
        report.put("highSeverity", high);
        report.put("mediumSeverity", medium);
        report.put("lowSeverity", low);

        return report;
    }
}