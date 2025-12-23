package com.sstu.LearningManagementSystem.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Утильный класс для работы с JWT токенами.
 * Отвечает за генерацию, извлечение данных и валидацию токенов.
 */
@Component
public class JwtUtil {

    @Value("${spring.security.jwt.secret}")
    private String secretKey;

    @Value("${spring.security.jwt.expiration}")
    private long expiration; // в ms, e.g., 86400000 в properties

    /**
     * Извлекает имя пользователя из токена.
     *
     * @param token JWT токен.
     * @return Имя пользователя.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Извлекает дату истечения токена.
     *
     * @param token JWT токен.
     * @return Дата истечения.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Извлекает произвольное утверждение (claim) из токена.
     *
     * @param token           JWT токен.
     * @param claimsResolver  Функция для извлечения конкретного claim.
     * @param <T>             Тип возвращаемого claim.
     * @return Значение claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Извлекает все утверждения (claims) из токена.
     *
     * @param token JWT токен.
     * @return Claims объект.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }

    /**
     * Проверяет, истек ли срок действия токена.
     *
     * @param token JWT токен.
     * @return true, если токен истек.
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Генерирует JWT токен для пользователя.
     *
     * @param userDetails Данные пользователя для генерации токена.
     * @return Сгенерированный JWT токен.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Добавьте роль или другие claims если нужно
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Создает JWT токен с указанными утверждениями и сроком действия.
     *
     * @param claims  Утверждения (claims) для включения в токен.
     * @param subject Имя пользователя (subject) для токена.
     * @return Сгенерированный JWT токен.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * Валидирует JWT токен: проверяет имя пользователя и срок действия.
     *
     * @param token       JWT токен для валидации.
     * @param userDetails Данные пользователя, для которого проверяется токен.
     * @return true, если токен действителен.
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}