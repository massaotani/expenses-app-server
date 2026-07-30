package com.expensesapp.server.service.payment;

import java.util.List;

import com.expensesapp.server.dto.PaymentMethodRequest;
import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.PaymentMethod;

public interface PaymentMethodService {
    List<PaymentMethod> getAllMethodsForUser(AuthUser authUser);

    PaymentMethod createPaymentMethod(PaymentMethodRequest request, AuthUser authUser);
}
