package com.example.projectmanager.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@project-manager.local}")
    private String from;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendVerificationEmail(String to, String username, String token) {
        String link = baseUrl + "/confirm?token=" + token;

        String text = "Здравствуйте, " + username + "!\n\n" +
                "Вы зарегистрировались в системе управления проектами.\n" +
                "Для подтверждения email перейдите по ссылке:\n" +
                link + "\n\n" +
                "Ссылка действительна 24 часа.\n\n" +
                "С уважением, команда Проектного менеджера";

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject("Подтверждение регистрации — Проектный менеджер");
            msg.setText(text);
            mailSender.send(msg);
            System.out.println(">>> Письмо отправлено на " + to);
        } catch (Exception e) {
            System.out.println("==============================================");
            System.out.println("ССЫЛКА ДЛЯ ПОДТВЕРЖДЕНИЯ РЕГИСТРАЦИИ:");
            System.out.println(link);
            System.out.println("==============================================");
        }
    }

    public void sendNotification(String to, String subject, String text) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);
        } catch (Exception e) {
            System.out.println("[EMAIL] To: " + to + " | " + subject + "\n" + text);
        }
    }
}