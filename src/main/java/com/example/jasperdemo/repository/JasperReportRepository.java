package com.example.jasperdemo.repository;


import com.example.jasperdemo.domain.JasperReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the JasperReport entity.
 */
@SuppressWarnings("unused")
@Repository
public interface JasperReportRepository extends JpaRepository<JasperReport, Long> {}
