package com.expensesapp.server.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    
    // Explicit @Query required when using @Modifying
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM RefreshToken r WHERE r.authUser = :authUser")
    void deleteByAuthUser(@Param("authUser") AuthUser authUser);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < :now")
    int deleteAllExpiredTokens(@Param("now") Instant now);
}