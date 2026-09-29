package com.taxgap.taxgapdetection.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taxgap.taxgapdetection.service.ReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/customer-tax-summary")
    public ResponseEntity<Map<String, Object>> getCustomerTaxSummary(
            @RequestParam String customerId) {

        return ResponseEntity.ok(
                reportService.getCustomerTaxSummary(customerId)
        );
    }

    @GetMapping("/exception-summary")
    public ResponseEntity<Map<String, Object>> getExceptionSummary() {

        return ResponseEntity.ok(
                reportService.getExceptionSummary()
        );
    }
}