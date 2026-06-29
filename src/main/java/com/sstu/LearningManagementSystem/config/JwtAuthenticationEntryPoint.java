package com.sstu.LearningManagementSystem.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Обработчик исключений Spring Security для случаев, когда аутентификация не пройдена.
 * Отправляет HTTP 401 Unauthorized ответ клиенту.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    /**
     * Метод, вызываемый, когда пользователь пытается получить доступ к защищенному ресурсу без аутентификации.
     * Устанавливает статус 401 и отправляет JSON-ответ с сообщением об ошибке.
     *
     * @param request       HTTP-запрос.
     * @param response      HTTP-ответ.
     * @param authException Исключение аутентификации.
     * @throws IOException
     */
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"error\": \"Unauthorized: " + authException.getMessage() + "\"}");
    }
}
