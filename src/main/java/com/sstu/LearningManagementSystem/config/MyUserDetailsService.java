package com.sstu.LearningManagementSystem.config;

import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Реализация Spring Security интерфейса UserDetailsService.
 * Загружает данные пользователя по имени пользователя для аутентификации.
 * Проверяет статус верификации email перед предоставлением доступа.
 */
@Service
@RequiredArgsConstructor // Убедитесь, что Lombok генерирует конструктор
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; // Должно быть final

    @Override
    /**
     * Загружает UserDetails по имени пользователя.
     * Проверяет, подтвержден ли email пользователя. Если нет, выбрасывает исключение.
     *
     * @param username Имя пользователя для поиска.
     * @return UserDetails, если пользователь найден и верифицирован.
     * @throws UsernameNotFoundException если пользователь не найден или email не подтвержден.
     */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!u.isVerified()) { // <-- Эта проверка должна быть
            throw new UsernameNotFoundException("Email not verified");
        }

        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPassword(), // <-- Тут вызывается getPassword()
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()))
        );
    }
}