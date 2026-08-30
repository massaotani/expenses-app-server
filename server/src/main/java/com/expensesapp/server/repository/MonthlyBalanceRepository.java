package com.expensesapp.server.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expensesapp.server.model.MonthlyBalance;

public interface MonthlyBalanceRepository extends JpaRepository<MonthlyBalance, UUID> {
    Optional<MonthlyBalance> findByUser_IdAndYearAndMonth(UUID userId, Integer year, Integer month);
}