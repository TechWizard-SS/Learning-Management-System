package com.sstu.LearningManagementSystem.service;

import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.userDto.UserCreateDto;
import com.sstu.LearningManagementSystem.model.dto.userDto.UserResponseDto;
import com.sstu.LearningManagementSystem.model.dto.userDto.UserUpdateDto;
import com.sstu.LearningManagementSystem.model.enumType.Role;
import com.sstu.LearningManagementSystem.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Сервис для управления пользователями (User).
 * Обеспечивает создание, получение, обновление пользователей.
 * Содержит методы для поиска пользователей по ID и имени пользователя.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Создает нового пользователя.
     * Проверяет уникальность email. Шифрует пароль перед сохранением.
     *
     * @param createDto DTO с данными для создания пользователя.
     * @return DTO созданного пользователя.
     * @throws RuntimeException если email уже существует.
     */
    public UserResponseDto createUser(UserCreateDto createDto) {
        if (userRepository.existsByEmail(createDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(createDto.getUsername())
                .firstName(createDto.getFirstName())
                .lastName(createDto.getLastName())
                .email(createDto.getEmail())
                .phone(createDto.getPhone())
                .avatarUrl(createDto.getAvatarUrl())
                .registrationDate(LocalDateTime.now())
                .password(passwordEncoder.encode(createDto.getPassword()))
                .role(Role.STUDENT)
                .ratingPosition(0)
                .verified(false)
                .build();

        User savedUser = userRepository.save(user);
        return mapToResponseDto(savedUser);
    }

    /**
     * Получает пользователя по его ID.
     *
     * @param id ID пользователя.
     * @return DTO пользователя.
     * @throws EntityNotFoundException если пользователь с указанным ID не найден.
     */
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return mapToResponseDto(user);
    }

    /**
     * Обновляет данные пользователя по его ID.
     * Обновляет только ненулевые поля из DTO.
     *
     * @param id         ID пользователя для обновления.
     * @param updateDto  DTO с новыми данными пользователя.
     * @return DTO обновленного пользователя.
     * @throws EntityNotFoundException если пользователь с указанным ID не найден.
     */
    public UserResponseDto updateUser(Long id, UserUpdateDto updateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        if (updateDto.getUsername() != null) user.setUsername(updateDto.getUsername());
        if (updateDto.getFirstName() != null) user.setFirstName(updateDto.getFirstName());
        if (updateDto.getLastName() != null) user.setLastName(updateDto.getLastName());
        if (updateDto.getPhone() != null) user.setPhone(updateDto.getPhone());
        if (updateDto.getAvatarUrl() != null) user.setAvatarUrl(updateDto.getAvatarUrl());

        User updatedUser = userRepository.save(user);
        return mapToResponseDto(updatedUser);
    }

    /**
     * Находит пользователя по имени пользователя (username).
     * Используется в MyUserDetailsService.
     *
     * @param username Имя пользователя.
     * @return Сущность пользователя.
     * @throws UsernameNotFoundException если пользователь с указанным username не найден.
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Преобразует сущность User в UserResponseDto.
     * Включает все основные поля пользователя, а также даты создания и обновления из Auditable.
     *
     * @param user Сущность пользователя для преобразования.
     * @return DTO пользователя.
     */
    private UserResponseDto mapToResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setRegistrationDate(user.getRegistrationDate());
        dto.setRatingPosition(user.getRatingPosition());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    /**
     * Находит пользователя по ID.
     * Используется, например, в AuthController для получения сущности User после регистрации.
     *
     * @param id ID пользователя.
     * @return Сущность пользователя.
     * @throws EntityNotFoundException если пользователь с указанным ID не найден.
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }
}
