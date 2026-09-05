package com.expensesapp.server.service.email;

public interface EmailService {

    void sendResetCodeEmail(String toEmail, String code);

    void processForgotPassword(String email);

    void processResetPassword(String email, String code, String newPassword);

    void verifyResetCode(String email, String code);
}
