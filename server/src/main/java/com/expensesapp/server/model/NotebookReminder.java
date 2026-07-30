package com.expensesapp.server.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.expensesapp.server.model.enums.ReminderType;

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
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notebook_reminders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotebookReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @PositiveOrZero
    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "target_date", nullable = false)
    private LocalDateTime targetDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderType type;

    @NotNull
    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted;

    @NotNull
    @Column(name = "reminder_sent", nullable = false)
    private boolean reminderSent;

    @NotNull
    @Column(name = "lead_days", nullable = false)
    private Integer leadDays;

    @Column(name = "peer_name")
    private String peerName;
}