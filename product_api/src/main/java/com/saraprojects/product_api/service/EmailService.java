package com.saraprojects.product_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    public EmailService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Async
    public void sendEmployeeCodeEmail(String toEmail, String toName, String employeeCode) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            Map<String, Object> body = Map.of(
                    "sender", Map.of("name", senderName, "email", senderEmail),
                    "to", List.of(Map.of("email", toEmail, "name", toName)),
                    "subject", "Your employee access code",
                    "htmlContent", buildEmailContent(toName, employeeCode)
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(BREVO_API_URL, request, String.class);

        } catch (Exception e) {
            System.err.println("Failed to send employee code email: " + e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String toName, String resetToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            Map<String, Object> body = Map.of(
                    "sender", Map.of("name", senderName, "email", senderEmail),
                    "to", List.of(Map.of("email", toEmail, "name", toName)),
                    "subject", "Your password reset code",
                    "htmlContent", buildPasswordResetContent(toName, resetToken)
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(BREVO_API_URL, request, String.class);

        } catch (Exception e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
        }
    }

    private String buildEmailContent(String name, String employeeCode) {
        return "<p>Hi " + name + ",</p>"
                + "<p>Your employee access code is:</p>"
                + "<h2>" + employeeCode + "</h2>"
                + "<p>Use it together with your password to sign in.</p>";
    }

    private String buildPasswordResetContent(String name, String resetToken) {
        return "<p>Hi " + name + ",</p>"
                + "<p>You requested to reset your password. Use the code below:</p>"
                + "<h2>" + resetToken + "</h2>"
                + "<p>This code expires in 30 minutes. If you didn't request this, you can safely ignore this email.</p>";
    }

}