package com.expensesapp.server.service.income;

import java.util.List;
import java.util.UUID;

import com.expensesapp.server.dto.IncomeDepositRequest;
import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.Income;

public interface IncomeService {

    Income createIncome(IncomeDepositRequest request, AuthUser authUser);

    List<Income> getUserIncomes(AuthUser authUser);

    Income updateIncome(UUID incomeId, IncomeDepositRequest request, AuthUser authUser);

    void deleteIncome(UUID incomeId, AuthUser authUser);
}