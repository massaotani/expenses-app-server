package com.expensesapp.server.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.expensesapp.server.model.enums.ExpenseCategory;
import com.expensesapp.server.model.enums.PaymentType;
import com.expensesapp.server.model.enums.RecurrencePeriod;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    // @NotNull
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "payment_method_id", nullable = false)
    // @ToString.Exclude
    // private PaymentMethod paymentMethod;
    // @NotNull
    // @Enumerated(EnumType.STRING)
    // @Column(name = "payment_type", nullable = false)
    // private PaymentType paymentType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType; // CASH or CARD

    // Link an optional card relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = true) // Nullable because CASH doesn't use a card
    private Card card;

    @NotNull
    @Column(nullable = false)
    private String description;

    @NotNull
    @Positive(message = "Expense amount must be greater than zero")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @NotNull
    @Column(name = "is_paid", nullable = false)
    private boolean isPaid;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_period", nullable = false)
    private RecurrencePeriod recurrencePeriod;
}