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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Income createIncome(IncomeDepositRequest request, AuthUser authUser) {
        User user = userRepository.findById(authUser.getUserProfile().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found"));

        // 1. Create and persist the Income record
        Income income = new Income();
        income.setDescription(request.getDescription());
        income.setValue(request.getAmount());
        // income.setCategory(request.getCategory() != null ? request.getCategory() : "SALARY");
        // income.setPaymentType(request.getPaymentType() != null ? request.getPaymentType() : "CASH");
        income.setCreatedAt(LocalDateTime.now());
        income.setUser(user);

        Income savedIncome = incomeRepository.save(income);

        // 2. Increment total monthlyIncome on User
        BigDecimal currentIncome = user.getMonthlyIncome() != null ? user.getMonthlyIncome() : BigDecimal.ZERO;
        user.setMonthlyIncome(currentIncome.add(request.getAmount()));
        userRepository.save(user);

        return savedIncome;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Income> getUserIncomes(AuthUser authUser) {
        return incomeRepository.findByUserIdOrderByCreatedAtDesc(authUser.getUserProfile().getId());
    }

    @Override
    @Transactional
    public void deleteIncome(UUID incomeId, AuthUser authUser) {
        Income income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Income entry not found"));

        if (!income.getUser().getId().equals(authUser.getUserProfile().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        // Subtract value from User's total monthly income
        User user = income.getUser();
        BigDecimal currentIncome = user.getMonthlyIncome() != null ? user.getMonthlyIncome() : BigDecimal.ZERO;
        BigDecimal updatedIncome = currentIncome.subtract(income.getValue());
        user.setMonthlyIncome(updatedIncome.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : updatedIncome);
        userRepository.save(user);

        incomeRepository.delete(income);
    }
}
