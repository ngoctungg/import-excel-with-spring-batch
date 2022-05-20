package com.demo.import_excel.repository.jpa;

import com.demo.import_excel.repository.entity.TempEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TempEntityRepository extends JpaRepository<TempEntity, Long> {
}