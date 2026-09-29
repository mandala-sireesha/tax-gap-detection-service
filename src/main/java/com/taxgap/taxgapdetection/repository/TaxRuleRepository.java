package com.taxgap.taxgapdetection.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.taxgap.taxgapdetection.entity.TaxRule;

public interface TaxRuleRepository extends JpaRepository<TaxRule, Long> {

}