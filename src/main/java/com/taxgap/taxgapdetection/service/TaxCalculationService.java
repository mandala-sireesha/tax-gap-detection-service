package com.taxgap.taxgapdetection.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.taxgap.taxgapdetection.entity.Transaction;

@Service
public class TaxCalculationService {

    public void calculateTax(Transaction transaction) {

        // If reported tax is missing
        if (transaction.getReportedTax() == null) {
            transaction.setExpectedTax(
                    transaction.getAmount().multiply(transaction.getTaxRate())
            );

            transaction.setTaxGap(null);
            transaction.setComplianceStatus("NON_COMPLIANT");
            return;
        }

        // expectedTax = amount * taxRate
        BigDecimal expectedTax = transaction.getAmount()
                .multiply(transaction.getTaxRate());

        // taxGap = expectedTax - reportedTax
        BigDecimal taxGap = expectedTax
                .subtract(transaction.getReportedTax());

        transaction.setExpectedTax(expectedTax);
        transaction.setTaxGap(taxGap);

        // Compliance checking
        BigDecimal one = BigDecimal.ONE;

        if (taxGap.abs().compareTo(one) <= 0) {
            transaction.setComplianceStatus("COMPLIANT");

        } else if (taxGap.compareTo(one) > 0) {
            transaction.setComplianceStatus("UNDERPAID");

        } else if (taxGap.compareTo(one.negate()) < 0) {
            transaction.setComplianceStatus("OVERPAID");
        }
    }
}