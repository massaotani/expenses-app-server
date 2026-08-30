package com.expensesapp.server.service.balance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.MonthlyBalance;
import com.expensesapp.server.model.User;
import com.expensesapp.server.repository.MonthlyBalanceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MonthlyBalanceServiceImpl implements MonthlyBalanceService {

    private final MonthlyBalanceRepository monthlyBalanceRepository;

    @Override
    @Transactional
    public MonthlyBalance getOrCreateMonthlyBalance(User user, int year, int month) {
        return monthlyBalanceRepository.findByUser_IdAndYearAndMonth(user.getId(), year, month)
                .orElseGet(() -> {
                    BigDecimal baseIncome = user.getMonthlyIncome() != null ? user.getMonthlyIncome() : BigDecimal.ZERO;
                    MonthlyBalance newBalance = MonthlyBalance.builder()
                            .user(user)
                            .year(year)
                            .month(month)
                            .income(baseIncome)
                            .totalExpenses(BigDecimal.ZERO)
                            .savings(baseIncome)
                            .build();
                    return monthlyBalanceRepository.save(newBalance);
                });
    }

    @Override
    @Transactional
    public MonthlyBalance adjustIncome(User user, int year, int month, BigDecimal delta) {
        MonthlyBalance balance = getOrCreateMonthlyBalance(user, year, month);
        BigDecimal updatedIncome = balance.getIncome().add(delta);

        balance.setIncome(updatedIncome.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : updatedIncome);
        balance.setSavings(balance.getIncome().subtract(balance.getTotalExpenses()));

        return monthlyBalanceRepository.save(balance);
    }

    @Override
    @Transactional
    public MonthlyBalance getCurrentMonthBalance(AuthUser authUser) {
        if (authUser.getUserProfile() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User profile not found");
        }
        LocalDateTime now = LocalDateTime.now();
        return getOrCreateMonthlyBalance(authUser.getUserProfile(), now.getYear(), now.getMonthValue());
    }

    @Override
    @Transactional
    public MonthlyBalance getMonthBalance(AuthUser authUser, int year, int month) {
        if (authUser.getUserProfile() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User profile not found");
        }
        return getOrCreateMonthlyBalance(authUser.getUserProfile(), year, month);
    }
}
