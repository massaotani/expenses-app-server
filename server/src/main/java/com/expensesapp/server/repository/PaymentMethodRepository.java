package com.expensesapp.server.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.expensesapp.server.model.PaymentMethod;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    // Finds all wallets/cards associated with a specific user profile
    List<PaymentMethod> findByUser_Id(UUID userId);
}
