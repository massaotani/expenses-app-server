package com.expensesapp.server.service.email;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.PasswordResetCode;
import com.expensesapp.server.repository.AuthUserRepository;
import com.expensesapp.server.repository.PasswordResetCodeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final PasswordResetCodeRepository resetCodeRepository;
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Override
    public void sendResetCodeEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Password Reset Code");
        message.setText("Your password reset code is: " + code + "\nIt expires in 15 minutes.");
        mailSender.send(message);
    }

    @Override
    @Transactional
    public void processForgotPassword(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        String formattedEmail = email.trim().toLowerCase();

        if (authUserRepository.findByEmail(formattedEmail).isPresent()) {
            resetCodeRepository.deleteByEmail(formattedEmail);

            String code = String.format("%06d", new Random().nextInt(900000) + 100000);
            PasswordResetCode resetCode = PasswordResetCode.builder()
                    .email(formattedEmail)
                    .code(code)
                    .expiryDate(LocalDateTime.now().plusMinutes(15))
                    .build();

            resetCodeRepository.save(resetCode);
            sendResetCodeEmail(formattedEmail, code);
        }
    }

    @Override
    @Transactional
    public void processResetPassword(String email, String code, String newPassword) {
        if (email == null || code == null || newPassword == null) {
            throw new IllegalArgumentException("Missing required fields.");
        }

        String formattedEmail = email.trim().toLowerCase();
        PasswordResetCode resetCode = resetCodeRepository.findByEmailAndCode(formattedEmail, code.trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid code or email."));

        if (resetCode.getExpiryDate().isBefore(LocalDateTime.now())) {
            resetCodeRepository.delete(resetCode);
            throw new IllegalArgumentException("Code has expired. Request a new one.");
        }

        AuthUser user = authUserRepository.findByEmail(formattedEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        user.setPassword(passwordEncoder.encode(newPassword));
        authUserRepository.save(user);

        resetCodeRepository.delete(resetCode);
    }
}