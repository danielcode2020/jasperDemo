package com.example.jasperdemo.repository;


import com.example.jasperdemo.domain.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the DataSource entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DataSourceRepository extends JpaRepository<DataSource, Long> {
    Optional<DataSource> findFirstByLabel(String label);
    @Modifying
    void deleteAllByLabelContaining(String label);
}
