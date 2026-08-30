package com.expensesapp.server.model;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "monthly_balances",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "year_number", "month_number"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "year_number", nullable = false)
    private Integer year;

    @NotNull
    @Column(name = "month_number", nullable = false)
    private Integer month;

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal income;

    @NotNull
    @Column(name = "total_expenses", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalExpenses;

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal savings;
}