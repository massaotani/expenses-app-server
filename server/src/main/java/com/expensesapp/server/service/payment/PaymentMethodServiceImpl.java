package com.expensesapp.server.service.payment;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.expensesapp.server.dto.PaymentMethodRequest;
import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.PaymentMethod;
import com.expensesapp.server.model.enums.PaymentType;
import com.expensesapp.server.repository.PaymentMethodRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {
    private final PaymentMethodRepository paymentMethodRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethod> getAllMethodsForUser(AuthUser authUser) {
        // Enforce scenario guard: Admins do not have core financial profiles
        if (authUser.getUserProfile() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Admin accounts do not possess financial profiles.");
        }

        return paymentMethodRepository.findByUser_Id(authUser.getUserProfile().getId());
    }

    @Override
    @Transactional
    public PaymentMethod createPaymentMethod(PaymentMethodRequest request, AuthUser authUser) {
        if (authUser.getUserProfile() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin accounts cannot create payment methods.");
        }

        // Business Rule validation: Credit cards require an explicit credit limit set
        if (request.getType() == PaymentType.CREDIT && request.getCreditLimit() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A credit limit must be specified for credit card profiles.");
        }

        PaymentMethod paymentMethod = PaymentMethod.builder()
                .name(request.getName())
                .type(request.getType())
                .creditLimit(request.getType() == PaymentType.CREDIT ? request.getCreditLimit() : null)
                .currentBalance(request.getCurrentBalance())
                .user(authUser.getUserProfile()) // Links instrument directly to current user context
                .build();

        return paymentMethodRepository.save(paymentMethod);
    }
}
