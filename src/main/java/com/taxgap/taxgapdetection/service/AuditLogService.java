package com.taxgap.taxgapdetection.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.taxgap.taxgapdetection.entity.AuditLog;
import com.taxgap.taxgapdetection.repository.AuditLogRepository;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String eventType, String transactionId, String detailJson) {

        AuditLog auditLog = new AuditLog();

        auditLog.setEventType(eventType);
        auditLog.setTransactionId(transactionId);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setDetailJson(detailJson);

        auditLogRepository.save(auditLog);
    }
}