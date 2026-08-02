package com.expensesapp.server.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.expensesapp.server.model.Card;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
    // Derived query method to quickly retrieve all cards belonging to a specific
    // user profile
    List<Card> findByUser_Id(UUID userId);
}