package com.taxgap.taxgapdetection.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.taxgap.taxgapdetection.entity.ExceptionRecord;
import com.taxgap.taxgapdetection.repository.ExceptionRecordRepository;

@Service
public class ExceptionService {

    private final ExceptionRecordRepository exceptionRecordRepository;

    public ExceptionService(ExceptionRecordRepository exceptionRecordRepository) {
        this.exceptionRecordRepository = exceptionRecordRepository;
    }

    public void createException(
            String transactionId,
            String customerId,
            String ruleName,
            String severity,
            String message) {

        ExceptionRecord exceptionRecord = new ExceptionRecord();

        exceptionRecord.setTransactionId(transactionId);
        exceptionRecord.setCustomerId(customerId);
        exceptionRecord.setRuleName(ruleName);
        exceptionRecord.setSeverity(severity);
        exceptionRecord.setMessage(message);
        exceptionRecord.setTimestamp(LocalDateTime.now());

        exceptionRecordRepository.save(exceptionRecord);
    }
}