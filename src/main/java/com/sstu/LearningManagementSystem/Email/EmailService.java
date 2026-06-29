package com.sstu.LearningManagementSystem.Email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Сервис для отправки электронных писем.
 * Использует Spring Mail для отправки писем через SMTP-сервер, настроенный в application.properties.
 * Отправляет письма для подтверждения регистрации и сброса пароля.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Отправляет письмо с ссылкой для подтверждения регистрации.
     *
     * @param to    Email адрес получателя.
     * @param token Токен подтверждения, который будет вставлен в ссылку.
     */
    public void sendVerificationEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Подтверждение регистрации в LMS");
        message.setText("Для подтверждения перейдите по ссылке: http://localhost:7070/api/auth/verify?token=" + token +
                "\nСсылка действительна 2 часа.");
        mailSender.send(message);
    }

    /**
     * Отправляет письмо со ссылкой для сброса пароля.
     *
     * @param to    Email адрес получателя.
     * @param token Токен сброса пароля, который будет вставлен в ссылку.
     */
    public void sendResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Сброс пароля в LMS");
        message.setText("Для сброса пароля перейдите по ссылке: http://localhost:7070/api/auth/reset?token=" + token +
                "\nСсылка действительна 1 час.");
        mailSender.send(message);
    }
}