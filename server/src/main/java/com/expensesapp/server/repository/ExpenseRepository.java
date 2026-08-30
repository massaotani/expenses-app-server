package com.expensesapp.server.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.expensesapp.server.model.Expense;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    // Fetch every expense recorded by a specific user profile (sorted newest first)
    List<Expense> findByUser_IdOrderByDueDateDesc(UUID userId);

    List<Expense> findByCard_Id(UUID cardId);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND YEAR(e.dueDate) = :year AND MONTH(e.dueDate) = :month ORDER BY e.dueDate DESC")
    List<Expense> findByUserIdAndYearAndMonth(@Param("userId") UUID userId, @Param("year") int year, @Param("month") int month);
    // Fetch all transactions charged to a specific credit card or wallet
    // List<Expense> findByPaymentMethod_IdOrderByDueDateDesc(UUID paymentMethodId);
}
