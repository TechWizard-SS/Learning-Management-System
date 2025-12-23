package com.sstu.LearningManagementSystem.service;

import com.sstu.LearningManagementSystem.ForbiddenException;
import com.sstu.LearningManagementSystem.model.Assignment;
import com.sstu.LearningManagementSystem.model.Topic;
import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.assignmentDto.AssignmentCreateDto;
import com.sstu.LearningManagementSystem.model.dto.assignmentDto.AssignmentUpdateDto;
import com.sstu.LearningManagementSystem.model.dto.assignmentDto.AssignmentResponseDto;
import com.sstu.LearningManagementSystem.model.enumType.AssignmentType;
import com.sstu.LearningManagementSystem.model.enumType.Role;
import com.sstu.LearningManagementSystem.model.enumType.ContentType;
import com.sstu.LearningManagementSystem.repository.AssignmentRepository;
import com.sstu.LearningManagementSystem.repository.TopicRepository;
import com.sstu.LearningManagementSystem.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления заданиями (Assignment).
 * Обеспечивает создание, обновление, получение и удаление заданий, связанных с темами.
 * Проверяет права доступа к заданиям (только преподаватель/админ/владелец могут редактировать).
 */
@Service // Указывает Spring, что это компонент бизнес-логики
@RequiredArgsConstructor // Генерирует конструктор для final полей
@Transactional(readOnly = true) // Применяется ко всем методам по умолчанию для операций чтения
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final TopicRepository topicRepository; // Для получения Topic по ID при создании/обновлении
    private final UserRepository userRepository; // Для проверки прав доступа


    /**
     * Создает новое задание.
     * Требует проверки прав доступа (например, только TEACHER, ADMIN, OWNER).
     *
     * @param currentUserId ID пользователя, инициирующего действие.
     * @param createDto     DTO с данными для создания задания.
     * @return DTO созданного задания.
     * @throws EntityNotFoundException если тема не найдена.
     * @throws ForbiddenException      если у пользователя недостаточно прав.
     */
    @Transactional // Переопределяет readOnly для методов записи
    public AssignmentResponseDto createAssignment(Long currentUserId, AssignmentCreateDto createDto) {
        validateUserCanEditAssignments(currentUserId); // Проверка прав

        Topic topic = topicRepository.findById(createDto.getTopicId())
                .orElseThrow(() -> new EntityNotFoundException("Topic not found with id: " + createDto.getTopicId()));

        Assignment assignment = Assignment.builder()
                .title(createDto.getTitle())
                .description(createDto.getDescription())
                .content(createDto.getContent())
                .type(AssignmentType.valueOf(createDto.getType().toUpperCase()))
                .contentType(ContentType.valueOf(createDto.getContentType().toUpperCase()))
                .deadline(createDto.getDeadline())
                .topic(topic) // Устанавливаем связь с Topic
                .build();

        Assignment savedAssignment = assignmentRepository.save(assignment);

        // Перезапрашиваем Assignment с Topic, чтобы избежать LazyInit в toDto
        Assignment savedWithTopic = assignmentRepository.findByIdWithTopic(savedAssignment.getId())
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found after creation")); // На всякий случай

        return toDto(savedWithTopic); // <-- Теперь toDto не вызывает LazyInit
    }

    /**
     * Обновляет существующее задание.
     * Требует проверки прав доступа (например, только TEACHER, ADMIN, OWNER).
     *
     * @param currentUserId ID пользователя, инициирующего действие.
     * @param assignmentId  ID задания для обновления.
     * @param updateDto     DTO с новыми данными.
     * @return DTO обновленного задания.
     * @throws EntityNotFoundException если задание не найдено.
     * @throws ForbiddenException      если у пользователя недостаточно прав.
     * @throws IllegalArgumentException если передан неверный тип задания или типа контента.
     */
    @Transactional // Переопределяет readOnly для методов записи
    public AssignmentResponseDto updateAssignment(Long currentUserId, Long assignmentId, AssignmentUpdateDto updateDto) {
        validateUserCanEditAssignments(currentUserId); // Проверка прав

        // Используем метод с JOIN FETCH
        Assignment assignment = assignmentRepository.findByIdWithTopic(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + assignmentId));

        // Частичное обновление: обновляем только ненулевые поля из DTO
        if (updateDto.getTitle() != null) assignment.setTitle(updateDto.getTitle());
        if (updateDto.getDescription() != null) assignment.setDescription(updateDto.getDescription());
        if (updateDto.getContent() != null) assignment.setContent(updateDto.getContent());
        if (updateDto.getType() != null) {
            try {
                assignment.setType(AssignmentType.valueOf(updateDto.getType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid AssignmentType: " + updateDto.getType());
            }
        }
        if (updateDto.getContentType() != null) {
            try {
                assignment.setContentType(ContentType.valueOf(updateDto.getContentType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid ContentType: " + updateDto.getContentType());
            }
        }
        if (updateDto.getDeadline() != null) assignment.setDeadline(updateDto.getDeadline());

        Assignment updatedAssignment = assignmentRepository.save(assignment);

        // Перезапрашиваем обновленный Assignment с Topic, чтобы избежать LazyInit в toDto
        Assignment updatedWithTopic = assignmentRepository.findByIdWithTopic(updatedAssignment.getId()) // <-- Добавлено
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found after update")); // На всякий случай

        return toDto(updatedWithTopic); // <-- Теперь toDto не вызывает LazyInit
    }


    @Transactional // Переопределяет readOnly для методов записи
    public void deleteAssignment(Long currentUserId, Long assignmentId) {
        validateUserCanEditAssignments(currentUserId); // Проверка прав

        if (!assignmentRepository.existsById(assignmentId)) {
            throw new EntityNotFoundException("Assignment not found with id: " + assignmentId);
        }
        assignmentRepository.deleteById(assignmentId);
    }

    /**
     * Получает задание по ID.
     * Проверяет, что пользователь существует (аутентифицирован).
     *
     * @param currentUserId ID пользователя, инициирующего действие.
     * @param assignmentId  ID задания для получения.
     * @return DTO запрашиваемого задания.
     * @throws EntityNotFoundException если пользователь или задание не найдены.
     */
    public AssignmentResponseDto getAssignmentById(Long currentUserId, Long assignmentId) {
        // Проверяем, что пользователь существует (аутентификация)
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Используем метод с JOIN FETCH
        Assignment assignment = assignmentRepository.findByIdWithTopic(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + assignmentId));

        return toDto(assignment); // <-- Теперь toDto не вызывает LazyInit
    }

    /**
     * Находит все задания, связанные с определенной темой.
     *
     * @param currentUserId ID пользователя, инициирующего действие.
     * @param topicId       ID темы.
     * @return Список DTO заданий.
     * @throws EntityNotFoundException если пользователь или тема не найдены.
     */
    public List<AssignmentResponseDto> findAssignmentsByTopicId(Long currentUserId, Long topicId) {
        // Проверяем, что пользователь существует (аутентификация)
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Проверяем, что тема существует
        topicRepository.findById(topicId)
                .orElseThrow(() -> new EntityNotFoundException("Topic not found with id: " + topicId));

        // Используем метод с JOIN FETCH
        List<Assignment> assignments = assignmentRepository.findByTopicIdWithTopic(topicId);
        return assignments.stream()
                .map(this::toDto) // <-- Теперь toDto не вызывает LazyInit для каждого элемента
                .collect(Collectors.toList());
    }


    // --- Вспомогательные методы ---

    /**
     * Преобразует сущность Assignment в AssignmentResponseDto.
     * Включает ID, заголовок, описание, содержимое, тип, тип контента, дедлайн, ID темы и даты создания/обновления.
     *
     * @param assignment Сущность задания для преобразования.
     * @return DTO задания.
     */
    private AssignmentResponseDto toDto(Assignment assignment) {
        AssignmentResponseDto dto = new AssignmentResponseDto();
        dto.setId(assignment.getId());
        dto.setTitle(assignment.getTitle());
        dto.setDescription(assignment.getDescription());
        dto.setContent(assignment.getContent());
        dto.setType(assignment.getType() != null ? assignment.getType().name() : null); // Преобразуем enum в строку
        dto.setContentType(assignment.getContentType() != null ? assignment.getContentType().name() : null); // Преобразуем enum в строку
        dto.setDeadline(assignment.getDeadline());
        dto.setCreatedAt(assignment.getCreatedAt());
        dto.setUpdatedAt(assignment.getUpdatedAt());
        // Убедитесь, что assignment.getTopic() загружен (JOIN FETCH в репозитории)
        if (assignment.getTopic() != null) {
            dto.setTopicId(assignment.getTopic().getId());
        }
        return dto;
    }

    /**
     * Проверяет, имеет ли пользователь право редактировать задания (создание, обновление, удаление).
     * Выбрасывает ForbiddenException, если у пользователя недостаточно прав.
     *
     * @param userId ID пользователя для проверки.
     * @throws EntityNotFoundException если пользователь не найден.
     * @throws ForbiddenException      если доступ запрещен.
     */
    private void validateUserCanEditAssignments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getRole() != Role.OWNER &&
                user.getRole() != Role.ADMIN &&
                user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only OWNER, ADMIN, or TEACHER can edit assignments");
        }
    }
}