package com.sstu.LearningManagementSystem.service;

import com.sstu.LearningManagementSystem.ForbiddenException;
import com.sstu.LearningManagementSystem.model.Achievement;
import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.achievementDto.AchievementCreateDto;
import com.sstu.LearningManagementSystem.model.dto.achievementDto.AchievementResponseDto;
import com.sstu.LearningManagementSystem.model.dto.achievementDto.AchievementUpdateDto;
import com.sstu.LearningManagementSystem.model.enumType.Role;
import com.sstu.LearningManagementSystem.repository.AchievementRepository;
import com.sstu.LearningManagementSystem.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository; // Может понадобиться, если добавлять/удалять из пользователей в этом сервисе

    /**
     * Создает новое достижение (тип).
     * Требует проверки прав (например, только TEACHER, ADMIN, OWNER).
     *
     * @param currentUserId ID пользователя, инициирующего создание.
     * @param createDto DTO с данными для создания достижения.
     * @return DTO созданного достижения.
     */
    @Transactional
    public AchievementResponseDto createAchievement(Long currentUserId, AchievementCreateDto createDto) {
        validateUserCanEditAchievements(currentUserId); // Проверка прав

        // Проверить, существует ли уже достижение с таким названием
        if (achievementRepository.findByTitle(createDto.getTitle()).isPresent()) {
            throw new IllegalArgumentException("Achievement with title '" + createDto.getTitle() + "' already exists.");
        }

        Achievement achievement = Achievement.builder()
                .title(createDto.getTitle())
                .description(createDto.getDescription())
                .iconUrl(createDto.getIconUrl())
                .build();

        Achievement savedAchievement = achievementRepository.save(achievement);
        return toDto(savedAchievement);
    }

    /**
     * Обновляет существующее достижение (тип).
     * Требует проверки прав (например, только TEACHER, ADMIN, OWNER).
     *
     * @param currentUserId ID пользователя, инициирующего обновление.
     * @param achievementId ID достижения для обновления.
     * @param updateDto     DTO с новыми данными.
     * @return DTO обновленного достижения.
     */
    @Transactional
    public AchievementResponseDto updateAchievement(Long currentUserId, Long achievementId, AchievementUpdateDto updateDto) {
        validateUserCanEditAchievements(currentUserId); // Проверка прав

        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new EntityNotFoundException("Achievement not found with id: " + achievementId));

        // Частичное обновление: обновляем только ненулевые поля из DTO
        if (updateDto.getTitle() != null) {
            // Проверить, не конфликтует ли новое название с существующим
            if (!achievement.getTitle().equals(updateDto.getTitle())) { // Только если название меняется
                if (achievementRepository.findByTitle(updateDto.getTitle()).isPresent()) {
                    throw new IllegalArgumentException("Achievement with title '" + updateDto.getTitle() + "' already exists.");
                }
            }
            achievement.setTitle(updateDto.getTitle());
        }
        if (updateDto.getDescription() != null) achievement.setDescription(updateDto.getDescription());
        if (updateDto.getIconUrl() != null) achievement.setIconUrl(updateDto.getIconUrl());

        Achievement updatedAchievement = achievementRepository.save(achievement);
        return toDto(updatedAchievement);
    }

    /**
     * Удаляет достижение по ID.
     * Требует проверки прав (например, только TEACHER, ADMIN, OWNER).
     * ВНИМАНИЕ: Удаление достижения не удаляет его из пользователей!
     *
     * @param currentUserId ID пользователя, инициирующего удаление.
     * @param achievementId ID достижения для удаления.
     */
    @Transactional
    public void deleteAchievement(Long currentUserId, Long achievementId) {
        validateUserCanEditAchievements(currentUserId); // Проверка прав

        if (!achievementRepository.existsById(achievementId)) {
            throw new EntityNotFoundException("Achievement not found with id: " + achievementId);
        }
        // Если используется мягкое удаление (soft-delete) через Auditable,
        // вместо repository.delete() установите deletedAt и сохраните.
        // Иначе, выполняем жесткое удаление:
        achievementRepository.deleteById(achievementId);
    }

    /**
     * Получает достижение по ID.
     *
     * @param currentUserId ID пользователя, инициирующего получение.
     * @param achievementId ID достижения для получения.
     * @return DTO запрашиваемого достижения.
     */
    public AchievementResponseDto getAchievementById(Long currentUserId, Long achievementId) {
        // Проверяем, что пользователь существует (аутентификация)
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new EntityNotFoundException("Achievement not found with id: " + achievementId));

        return toDto(achievement);
    }

    /**
     * Получает достижение по названию.
     *
     * @param currentUserId ID пользователя, инициирующего получение.
     * @param title         Название достижения.
     * @return DTO запрашиваемого достижения.
     */
    public AchievementResponseDto getAchievementByTitle(Long currentUserId, String title) {
        // Проверяем, что пользователь существует (аутентификация)
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Achievement achievement = achievementRepository.findByTitle(title)
                .orElseThrow(() -> new EntityNotFoundException("Achievement not found with title: " + title));

        return toDto(achievement);
    }

    /**
     * Получает все достижения.
     * Требует проверки прав (например, только TEACHER, ADMIN, OWNER или все пользователи).
     * Для простоты, разрешим всем аутентифицированным пользователям просматривать список.
     *
     * @param currentUserId ID пользователя, инициирующего получение.
     * @return Список DTO достижений.
     */
    public List<AchievementResponseDto> getAllAchievements(Long currentUserId) {
        // Проверяем, что пользователь существует (аутентификация)
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<Achievement> achievements = achievementRepository.findAll();
        return achievements.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // --- Вспомогательные методы ---

    /**
     * Преобразует сущность Achievement в AchievementResponseDto.
     *
     * @param achievement Сущность достижения.
     * @return DTO достижения.
     */
    private AchievementResponseDto toDto(Achievement achievement) {
        AchievementResponseDto dto = new AchievementResponseDto();
        dto.setId(achievement.getId());
        dto.setTitle(achievement.getTitle());
        dto.setDescription(achievement.getDescription());
        dto.setIconUrl(achievement.getIconUrl());
        return dto;
    }

    /**
     * Проверяет, имеет ли пользователь право редактировать достижения (создание, обновление, удаление).
     * Выбрасывает ForbiddenException, если у пользователя недостаточно прав.
     *
     * @param userId ID пользователя для проверки.
     */
    private void validateUserCanEditAchievements(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getRole() != Role.OWNER && // Убедитесь, что Role существует
                user.getRole() != Role.ADMIN &&
                user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only OWNER, ADMIN, or TEACHER can edit achievements");
        }
    }
}