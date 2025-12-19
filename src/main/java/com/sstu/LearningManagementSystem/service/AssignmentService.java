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
                .type(AssignmentType.valueOf(createDto.getType().toUpperCase())) // Убедитесь, что AssignmentType существует
                .contentType(ContentType.valueOf(createDto.getContentType().toUpperCase())) // Убедитесь, что ContentType существует
                .deadline(createDto.getDeadline()) // Если поле deadline добавлено в сущность
                .topic(topic) // Устанавливаем связь с Topic
                .build();

        Assignment savedAssignment = assignmentRepository.save(assignment);
        return toDto(savedAssignment);
    }

    /**
     * Обновляет существующее задание.
     * Требует проверки прав доступа (например, только TEACHER, ADMIN, OWNER).
     *
     * @param currentUserId ID пользователя, инициирующего действие.
     * @param assignmentId  ID задания для обновления.
     * @param updateDto     DTO с новыми данными.
     * @return DTO обновленного задания.
     */
    @Transactional // Переопределяет readOnly для методов записи
    public AssignmentResponseDto updateAssignment(Long currentUserId, Long assignmentId, AssignmentUpdateDto updateDto) {
        validateUserCanEditAssignments(currentUserId); // Проверка прав

        Assignment assignment = assignmentRepository.findById(assignmentId)
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
        return toDto(updatedAssignment);
    }


    @Transactional // Переопределяет readOnly для методов записи
    public void deleteAssignment(Long currentUserId, Long assignmentId) {
        validateUserCanEditAssignments(currentUserId); // Проверка прав

        if (!assignmentRepository.existsById(assignmentId)) {
            throw new EntityNotFoundException("Assignment not found with id: " + assignmentId);
        }
        // Если используется мягкое удаление (soft-delete) через Auditable,
        // вместо repository.delete() установите deletedAt и сохраните.
        // Иначе, выполняем жесткое удаление:
        assignmentRepository.deleteById(assignmentId);
    }

    /**
     * Получает задание по ID.
     * Проверяет, что пользователь существует (аутентифицирован).
     *
     * @param currentUserId ID пользователя, инициирующего действие.
     * @param assignmentId  ID задания для получения.
     * @return DTO запрашиваемого задания.
     */
    public AssignmentResponseDto getAssignmentById(Long currentUserId, Long assignmentId) {
        // Проверяем, что пользователь существует (аутентификация)
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + assignmentId));

        return toDto(assignment);
    }

    /**
     * Находит все задания, связанные с определенной темой.
     *
     * @param currentUserId ID пользователя, инициирующего действие.
     * @param topicId       ID темы.
     * @return Список DTO заданий.
     */
    public List<AssignmentResponseDto> findAssignmentsByTopicId(Long currentUserId, Long topicId) {
        // Проверяем, что пользователь существует (аутентификация)
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Проверяем, что тема существует
        topicRepository.findById(topicId)
                .orElseThrow(() -> new EntityNotFoundException("Topic not found with id: " + topicId));

        List<Assignment> assignments = assignmentRepository.findByTopicId(topicId);
        return assignments.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    // --- Вспомогательные методы ---

    /**
     * Преобразует сущность Assignment в AssignmentResponseDto.
     *
     * @param assignment Сущность задания.
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
        // Устанавливаем ID темы, к которой принадлежит задание
        if (assignment.getTopic() != null) {
            dto.setTopicId(assignment.getTopic().getId());
        }
        // Установите другие поля DTO, если необходимо (например, createdBy, updatedBy)
        // dto.setCreatedBy(assignment.getCreatedBy());
        // dto.setUpdatedBy(assignment.getUpdatedBy());
        return dto;
    }

    /**
     * Проверяет, имеет ли пользователь право редактировать задания (создание, обновление, удаление).
     * Выбрасывает ForbiddenException, если у пользователя недостаточно прав.
     *
     * @param userId ID пользователя для проверки.
     */
    private void validateUserCanEditAssignments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getRole() != Role.OWNER && // Убедитесь, что Role существует
                user.getRole() != Role.ADMIN &&
                user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only OWNER, ADMIN, or TEACHER can edit assignments");
        }
    }
}