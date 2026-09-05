package com.expensesapp.server.service.email;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

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

    @Value("${RESEND_API_KEY:}")
    private String resendApiKey;

    @Override
    public void sendResetCodeEmail(String toEmail, String code) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            throw new IllegalStateException("RESEND_API_KEY environment variable is not configured.");
        }

        String htmlContent = "<!DOCTYPE html>"
                + "<html>"
                + "<head><meta charset='utf-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>"
                + "<body style='margin:0; padding:0; background-color:#F4F6F8; font-family:-apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif;'>"
                + "  <table role='presentation' width='100%' cellspacing='0' cellpadding='0' style='background-color:#F4F6F8; padding: 40px 10px;'>"
                + "    <tr>"
                + "      <td align='center'>"
                + "        <table role='presentation' width='100%' cellspacing='0' cellpadding='0' style='max-width:500px; background-color:#FFFFFF; border-radius:16px; overflow:hidden; box-shadow:0 4px 12px rgba(0,0,0,0.05); border: 1px solid #E5E7EB;'>"
                + "          <tr>"
                + "            <td style='background-color:#0D9488; padding:32px 24px; text-align:center;'>"
                + "              <h1 style='color:#FFFFFF; margin:0; font-size:22px; font-weight:800; letter-spacing:2px;'>LEDGER</h1>"
                + "            </td>"
                + "          </tr>"
                + "          <tr>"
                + "            <td style='padding:32px 32px 24px 32px; color:#1F2937;'>"
                + "              <h2 style='margin:0 0 12px 0; font-size:18px; font-weight:700; color:#111827;'>Password Reset Request</h2>"
                + "              <p style='margin:0 0 24px 0; font-size:14px; line-height:1.6; color:#4B5563;'>"
                + "                We received a request to reset the password for your <strong>Ledger</strong> account. Use the verification code below to complete the process:"
                + "              </p>"
                + "              <div style='background-color:#F9FAFB; border:1px dashed #0D9488; border-radius:12px; padding:20px; text-align:center; margin-bottom:24px;'>"
                + "                <span style='display:inline-block; font-size:32px; font-weight:800; letter-spacing:8px; color:#0D9488; font-family:monospace;'>" + code + "</span>"
                + "              </div>"
                + "              <p style='margin:0 0 16px 0; font-size:13px; line-height:1.5; color:#6B7280;'>"
                + "                ⏱️ This code is valid for <strong>15 minutes</strong> and can only be used once."
                + "              </p>"
                + "              <p style='margin:0; font-size:13px; line-height:1.5; color:#9CA3AF; border-top:1px solid #F3F4F6; padding-top:16px;'>"
                + "                If you didn't request a password reset, you can safely ignore this email—your account remains secure."
                + "              </p>"
                + "            </td>"
                + "          </tr>"
                + "          <tr>"
                + "            <td style='background-color:#F9FAFB; padding:20px 32px; text-align:center; border-top:1px solid #E5E7EB;'>"
                + "              <p style='margin:0; font-size:12px; color:#9CA3AF;'>&copy; 2026 Ledger Financial App. All rights reserved.</p>"
                + "            </td>"
                + "          </tr>"
                + "        </table>"
                + "      </td>"
                + "    </tr>"
                + "  </table>"
                + "</body>"
                + "</html>";

        RestClient restClient = RestClient.create();

        restClient.post()
                .uri("https://api.resend.com/emails")
                .header("Authorization", "Bearer " + resendApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "from", "Ledger App <noreply@yourdomain.com>",
                        "to", new String[]{toEmail},
                        "subject", "Password Reset Code",
                        "html", htmlContent
                ))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    @Transactional
    public void processForgotPassword(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        String formattedEmail = email.trim().toLowerCase();

        authUserRepository.findByEmail(formattedEmail).ifPresent(user -> {
            resetCodeRepository.deleteByEmail(formattedEmail);

            String code = String.format("%06d", new Random().nextInt(900000) + 100000);
            PasswordResetCode resetCode = PasswordResetCode.builder()
                    .email(formattedEmail)
                    .code(code)
                    .expiryDate(LocalDateTime.now().plusMinutes(15))
                    .build();

            resetCodeRepository.save(resetCode);
            sendResetCodeEmail(formattedEmail, code);
        });
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

    @Override
    public void verifyResetCode(String email, String code) {
        if (email == null || code == null) {
            throw new IllegalArgumentException("Email and verification code are required.");
        }

        String formattedEmail = email.trim().toLowerCase();
        PasswordResetCode resetCode = resetCodeRepository.findByEmailAndCode(formattedEmail, code.trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification code."));

        if (resetCode.getExpiryDate().isBefore(LocalDateTime.now())) {
            resetCodeRepository.delete(resetCode);
            throw new IllegalArgumentException("Verification code has expired. Please request a new one.");
        }
    }
}
