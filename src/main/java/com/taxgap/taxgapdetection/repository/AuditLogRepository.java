package com.taxgap.taxgapdetection.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.taxgap.taxgapdetection.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

}