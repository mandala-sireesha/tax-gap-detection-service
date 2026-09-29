package com.taxgap.taxgapdetection.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taxgap.taxgapdetection.entity.ExceptionRecord;
import com.taxgap.taxgapdetection.repository.ExceptionRecordRepository;

@RestController
@RequestMapping("/api/exceptions")
public class ExceptionController {

    private final ExceptionRecordRepository exceptionRecordRepository;

    public ExceptionController(
            ExceptionRecordRepository exceptionRecordRepository) {

        this.exceptionRecordRepository = exceptionRecordRepository;
    }

    @GetMapping
    public ResponseEntity<List<ExceptionRecord>> getExceptions(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String ruleName) {

        List<ExceptionRecord> exceptions =
                exceptionRecordRepository.findAll()
                        .stream()
                        .filter(e -> customerId == null ||
                                customerId.equals(e.getCustomerId()))
                        .filter(e -> severity == null ||
                                severity.equalsIgnoreCase(e.getSeverity()))
                        .filter(e -> ruleName == null ||
                                ruleName.equalsIgnoreCase(e.getRuleName()))
                        .toList();

        return ResponseEntity.ok(exceptions);
    }
}