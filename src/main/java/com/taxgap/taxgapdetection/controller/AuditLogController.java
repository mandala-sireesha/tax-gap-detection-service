package com.taxgap.taxgapdetection.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taxgap.taxgapdetection.entity.AuditLog;
import com.taxgap.taxgapdetection.repository.AuditLogRepository;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {


private final AuditLogRepository auditLogRepository;

public AuditLogController(AuditLogRepository auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
}

@GetMapping
public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
    return ResponseEntity.ok(auditLogRepository.findAll());
}


}
