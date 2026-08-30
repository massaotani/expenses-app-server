package com.expensesapp.server.service.income;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.expensesapp.server.dto.IncomeDepositRequest;
import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.Income;
import com.expensesapp.server.model.User;
import com.expensesapp.server.repository.IncomeRepository;
import com.expensesapp.server.repository.UserRepository;
import com.expensesapp.server.service.balance.MonthlyBalanceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;
    private final MonthlyBalanceService monthlyBalanceService;

    @Override
    @Transactional
    public Income createIncome(IncomeDepositRequest request, AuthUser authUser) {
        User user = userRepository.findById(authUser.getUserProfile().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found"));

        Income income = new Income();
        income.setDescription(request.getDescription());
        income.setValue(request.getAmount());
        income.setCreatedAt(request.getCreatedAt() != null ? request.getCreatedAt() : LocalDateTime.now());
        income.setUser(user);

        Income savedIncome = incomeRepository.save(income);

        monthlyBalanceService.adjustIncome(
                user,
                savedIncome.getCreatedAt().getYear(),
                savedIncome.getCreatedAt().getMonthValue(),
                request.getAmount()
        );

        return savedIncome;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Income> getUserIncomes(AuthUser authUser) {
        return incomeRepository.findByUserIdOrderByCreatedAtDesc(authUser.getUserProfile().getId());
    }

    @Override
    @Transactional
    public Income updateIncome(UUID incomeId, IncomeDepositRequest request, AuthUser authUser) {
        Income income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Income entry not found"));

        if (!income.getUser().getId().equals(authUser.getUserProfile().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        BigDecimal delta = request.getAmount().subtract(income.getValue());

        income.setDescription(request.getDescription());
        income.setValue(request.getAmount());
        Income savedIncome = incomeRepository.save(income);

        monthlyBalanceService.adjustIncome(
                income.getUser(),
                income.getCreatedAt().getYear(),
                income.getCreatedAt().getMonthValue(),
                delta
        );

        return savedIncome;
    }

    @Override
    @Transactional
    public void deleteIncome(UUID incomeId, AuthUser authUser) {
        Income income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Income entry not found"));

        if (!income.getUser().getId().equals(authUser.getUserProfile().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        monthlyBalanceService.adjustIncome(
                income.getUser(),
                income.getCreatedAt().getYear(),
                income.getCreatedAt().getMonthValue(),
                income.getValue().negate()
        );

        incomeRepository.delete(income);
    }
}