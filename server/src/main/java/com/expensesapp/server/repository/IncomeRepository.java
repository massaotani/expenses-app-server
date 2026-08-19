package com.expensesapp.server.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.expensesapp.server.model.Income;

@Repository
public interface IncomeRepository extends JpaRepository<Income, UUID> {
    List<Income> findByUserIdOrderByCreatedAtDesc(UUID userId);
}