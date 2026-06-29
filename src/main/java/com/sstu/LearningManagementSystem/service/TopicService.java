package com.sstu.LearningManagementSystem.service;

import com.sstu.LearningManagementSystem.ForbiddenException;
import com.sstu.LearningManagementSystem.model.Module;
import com.sstu.LearningManagementSystem.model.Topic;
import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.topicDto.TopicCreateDto;
import com.sstu.LearningManagementSystem.model.dto.topicDto.TopicResponseDto;
import com.sstu.LearningManagementSystem.model.dto.topicDto.TopicUpdateDto;
import com.sstu.LearningManagementSystem.model.enumType.Role;
import com.sstu.LearningManagementSystem.repository.ModuleRepository;
import com.sstu.LearningManagementSystem.repository.TopicRepository;
import com.sstu.LearningManagementSystem.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Сервис для управления темами (Topic).
 * Обеспечивает создание, обновление, получение и удаление тем.
 * Проверяет права доступа к темам.
 */
@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;

    /**
     * Создает новую тему для указанного модуля.
     * Проверяет, что текущий пользователь имеет права на редактирование тем (OWNER, ADMIN, TEACHER).
     *
     * @param currentUserId ID пользователя, инициирующего создание.
     * @param dto           DTO с данными для создания темы.
     * @return DTO созданной темы.
     * @throws EntityNotFoundException если модуль не найден.
     * @throws ForbiddenException      если у пользователя нет прав на создание тем.
     */
    public TopicResponseDto createTopic(Long currentUserId, TopicCreateDto dto) {
        validateUserCanEditTopics(currentUserId);

        Module module = moduleRepository.findById(dto.getModuleId())
                .orElseThrow(() -> new EntityNotFoundException("Module not found"));

        Topic topic = Topic.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .contentType(dto.getContentType())
                .content(dto.getContent())
                .module(module)
                .build();

        Topic saved = topicRepository.save(topic);

        // Перезапрашиваем Topic с Module, чтобы избежать LazyInit в toDto
        Topic savedWithModule = topicRepository.findByIdWithModule(saved.getId())
                .orElseThrow(() -> new EntityNotFoundException("Topic not found after creation")); // На всякий случай

        return toDto(savedWithModule); // <-- Теперь toDto не вызывает LazyInit
    }

    /**
     * Обновляет существующую тему.
     * Проверяет, что текущий пользователь имеет права на редактирование тем (OWNER, ADMIN, TEACHER).
     *
     * @param currentUserId ID пользователя, инициирующего обновление.
     * @param topicId       ID темы для обновления.
     * @param dto           DTO с новыми данными темы.
     * @return DTO обновленной темы.
     * @throws EntityNotFoundException если тема не найдена.
     * @throws ForbiddenException      если у пользователя нет прав на обновление тем.
     */
    public TopicResponseDto updateTopic(Long currentUserId, Long topicId, TopicUpdateDto dto) {
        validateUserCanEditTopics(currentUserId);

        Topic topic = topicRepository.findByIdWithModule(topicId) // <-- Изменено
                .orElseThrow(() -> new EntityNotFoundException("Topic not found"));

        if (dto.getTitle() != null) topic.setTitle(dto.getTitle());
        if (dto.getDescription() != null) topic.setDescription(dto.getDescription());
        if (dto.getContent() != null) topic.setContent(dto.getContent());
        if (dto.getContentType() != null) topic.setContentType(dto.getContentType());

        Topic updated = topicRepository.save(topic);
        return toDto(updated); // <-- dto.setModuleId() теперь работает без LazyInit
    }

    /**
     * Получает тему по её ID.
     * Проверяет, что пользователь существует (аутентификация).
     *
     * @param currentUserId ID пользователя, инициирующего получение.
     * @param topicId       ID темы для получения.
     * @return DTO запрашиваемой темы.
     * @throws EntityNotFoundException если пользователь или тема не найдены.
     */
    //@Cacheable(value = "topics", key = "#topicId")
    public TopicResponseDto getTopicById(Long currentUserId, Long topicId) {
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Используйте метод, который загружает Module
        Topic topic = topicRepository.findByIdWithModule(topicId)
                .orElseThrow(() -> new EntityNotFoundException("Topic not found"));

        return toDto(topic); // <-- dto.setModuleId() теперь работает без LazyInit
    }

    /**
     * Удаляет тему по её ID.
     * Проверяет, что текущий пользователь имеет права на редактирование тем (OWNER, ADMIN, TEACHER).
     *
     * @param currentUserId ID пользователя, инициирующего удаление.
     * @param topicId       ID темы для удаления.
     * @throws EntityNotFoundException если тема не найдена.
     * @throws ForbiddenException      если у пользователя нет прав на удаление тем.
     */
    public void deleteTopic(Long currentUserId, Long topicId) {
        validateUserCanEditTopics(currentUserId);
        if (!topicRepository.existsById(topicId)) {
            throw new EntityNotFoundException("Topic not found");
        }
        topicRepository.deleteById(topicId);
    }

    /**
     * Преобразует сущность Topic в TopicResponseDto.
     * Включает ID, заголовок, описание, содержимое, тип содержимого, ID модуля и даты создания/обновления.
     *
     * @param topic Сущность темы для преобразования.
     * @return DTO темы.
     */
    private TopicResponseDto toDto(Topic topic) {
        TopicResponseDto dto = new TopicResponseDto();
        dto.setId(topic.getId());
        dto.setTitle(topic.getTitle());
        dto.setDescription(topic.getDescription());
        dto.setContent(topic.getContent());
        dto.setContentType(topic.getContentType());
        dto.setModuleId(topic.getModule().getId());
        dto.setCreatedAt(topic.getCreatedAt());
        dto.setUpdatedAt(topic.getUpdatedAt());
        return dto;
    }

    /**
     * Проверяет, имеет ли пользователь право редактировать темы (OWNER, ADMIN, TEACHER).
     * Выбрасывает ForbiddenException, если прав недостаточно.
     *
     * @param userId ID пользователя для проверки.
     * @throws EntityNotFoundException если пользователь не найден.
     * @throws ForbiddenException      если у пользователя нет прав на редактирование тем.
     */
    private void validateUserCanEditTopics(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getRole() != Role.OWNER &&
                user.getRole() != Role.ADMIN &&
                user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only OWNER, ADMIN or TEACHER can edit topics");
        }
    }
}
