package com.expensesapp.server.service.balance;

import java.math.BigDecimal;

import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.MonthlyBalance;
import com.expensesapp.server.model.User;

public interface MonthlyBalanceService {

    MonthlyBalance getOrCreateMonthlyBalance(User user, int year, int month);

    MonthlyBalance adjustIncome(User user, int year, int month, BigDecimal delta);

    MonthlyBalance getCurrentMonthBalance(AuthUser authUser);

    MonthlyBalance getMonthBalance(AuthUser authUser, int year, int month);
}
