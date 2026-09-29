package com.taxgap.taxgapdetection.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.taxgap.taxgapdetection.entity.ExceptionRecord;

public interface ExceptionRecordRepository extends JpaRepository<ExceptionRecord, Long> {

}