package com.sstu.LearningManagementSystem.service;

import com.sstu.LearningManagementSystem.ForbiddenException;
import com.sstu.LearningManagementSystem.model.Assignment;
import com.sstu.LearningManagementSystem.model.AssignmentSubmission;
import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.assignmentSubmissionDto.AssignmentSubmissionCreateDto;
import com.sstu.LearningManagementSystem.model.dto.assignmentSubmissionDto.AssignmentSubmissionResponseDto;
import com.sstu.LearningManagementSystem.model.enumType.Role;
import com.sstu.LearningManagementSystem.repository.AssignmentRepository;
import com.sstu.LearningManagementSystem.repository.AssignmentSubmissionRepository;
import com.sstu.LearningManagementSystem.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления сабмитами заданий (AssignmentSubmission).
 * Обеспечивает создание, обновление, получение и удаление сабмитов пользователей на задания.
 * Проверяет права доступа к сабмитам.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentSubmissionService {

    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final AssignmentRepository assignmentRepository; // Для проверки существования задания
    private final UserRepository userRepository; // Для проверки существования пользователя

    /**
     * Создает новый Submission или обновляет существующий при отправке ответа.
     * Увеличивает количество попыток. Проверяет, успешна ли попытка (в простом случае - по булевому флагу в DTO или автоматически).
     *
     * @param currentUserId ID пользователя, отправляющего ответ.
     * @param createDto DTO с информацией о задании и ответе.
     * @return DTO созданного/обновленного Submission.
     * @throws EntityNotFoundException если пользователь или задание не найдены.
     */
    @Transactional
    public AssignmentSubmissionResponseDto submitAnswer(Long currentUserId, AssignmentSubmissionCreateDto createDto) {
        // 1. Проверить, существует ли пользователь (аутентификация)
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + currentUserId));

        // 2. Проверить, существует ли задание
        Assignment assignment = assignmentRepository.findById(createDto.getAssignmentId())
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + createDto.getAssignmentId()));

        // 3. Найти существующий Submission для этого пользователя и задания
        // Используем метод с JOIN FETCH, если валидация требует
        AssignmentSubmission submission = assignmentSubmissionRepository
                .findByAssignmentIdAndUserIdWithUserAndAssignment(createDto.getAssignmentId(), currentUserId) // <-- Изменено
                .orElse(null);

        boolean isNewSubmission = (submission == null);

        if (isNewSubmission) {
            // 4. Если Submission не существует, создать новый
            submission = AssignmentSubmission.builder()
                    .user(user)
                    .assignment(assignment)
                    .answer(createDto.getAnswer())
                    .attempts(1) // Первая попытка
                    .passed(false) // Пока не проверено
                    .build();
        } else {
            // 5. Если Submission существует, увеличить attempts и обновить answer
            submission.setAttempts(submission.getAttempts() + 1);
            submission.setAnswer(createDto.getAnswer());
            // Не меняем passed, successfulAttempt, пока не проверим
        }

        // 6. Сохранить Submission
        AssignmentSubmission savedSubmission = assignmentSubmissionRepository.save(submission);

        // 7. Перезапрашиваем созданный/обновленный AssignmentSubmission с User и Assignment, чтобы избежать LazyInit в toDto
        AssignmentSubmission savedWithUserAndAssignment = assignmentSubmissionRepository.findByIdWithUserAndAssignment(savedSubmission.getId()) // <-- Добавлено
                .orElseThrow(() -> new EntityNotFoundException("Submission not found after creation/update")); // На всякий случай

        // 8. Вернуть DTO
        return toDto(savedWithUserAndAssignment); // <-- Теперь toDto не вызывает LazyInit
    }

    /**
     * Получить Submission по ID.
     * Требует проверки прав: пользователь может видеть только свой сабмит или преподаватель/админ - любой.
     *
     * @param currentUserId ID пользователя, запрашивающего сабмит.
     * @param submissionId  ID сабмита.
     * @return DTO сабмита.
     * @throws EntityNotFoundException если сабмит не найден.
     * @throws ForbiddenException      если доступ запрещен.
     */
    public AssignmentSubmissionResponseDto getSubmissionById(Long currentUserId, Long submissionId) {
        // Используем метод с JOIN FETCH
        AssignmentSubmission submission = assignmentSubmissionRepository.findByIdWithUserAndAssignment(submissionId) // <-- Изменено
                .orElseThrow(() -> new EntityNotFoundException("Submission not found with id: " + submissionId));

        // Проверить права доступа: текущий пользователь == владелец сабмита ИЛИ преподаватель/админ
        validateAccessToSubmission(currentUserId, submission.getUser().getId()); // <-- Добавлена проверка прав

        return toDto(submission); // <-- Теперь toDto не вызывает LazyInit
    }

    /**
     * Получить все Submission'ы пользователя по ID задания.
     * Требует проверки прав: пользователь запрашивает свои данные или преподаватель/админ.
     *
     * @param currentUserId ID пользователя, запрашивающего сабмиты.
     * @param assignmentId  ID задания.
     * @return Список DTO сабмитов.
     * @throws ForbiddenException если доступ запрещен.
     */
    public List<AssignmentSubmissionResponseDto> getSubmissionsByUserAndAssignmentId(Long currentUserId, Long assignmentId) {
        // Проверить права: currentUserId == запрашиваемый пользователь ИЛИ преподаватель/админ
        validateAccessToList(currentUserId); // <-- Добавлена проверка прав

        // Используем метод с JOIN FETCH
        List<AssignmentSubmission> submissions = assignmentSubmissionRepository.findByAssignmentIdAndUserIdWithUserAndAssignmentOrderByCreatedAtAsc(assignmentId, currentUserId); // <-- Изменено
        return submissions.stream()
                .map(this::toDto) // <-- Теперь toDto не вызывает LazyInit для каждого элемента
                .collect(Collectors.toList());
    }

    /**
     * Получить все Submission'ы для задания (например, для преподавателя).
     * Требует проверки прав: только преподаватель/админ/владелец курса.
     *
     * @param currentUserId ID пользователя, запрашивающего сабмиты.
     * @param assignmentId  ID задания.
     * @return Список DTO сабмитов.
     * @throws ForbiddenException если доступ запрещен.
     */
    public List<AssignmentSubmissionResponseDto> getSubmissionsByAssignmentId(Long currentUserId, Long assignmentId) {
        // Проверить права: currentUserId должен быть преподавателем/админом/владельцем курса
        validateUserCanViewSubmissions(currentUserId, assignmentId);

        // Используем метод с JOIN FETCH
        List<AssignmentSubmission> submissions = assignmentSubmissionRepository.findByAssignmentIdWithUserAndAssignment(assignmentId); // <-- Изменено
        return submissions.stream()
                .map(this::toDto) // <-- Теперь toDto не вызывает LazyInit для каждого элемента
                .collect(Collectors.toList());
    }

    // --- Вспомогательные методы ---

    /**
     * Преобразует сущность AssignmentSubmission в AssignmentSubmissionResponseDto.
     * Включает ID, ID пользователя, ID задания, количество попыток, успешна ли попытка, ответ и дату создания.
     *
     * @param submission Сущность сабмита для преобразования.
     * @return DTO сабмита.
     */
    private AssignmentSubmissionResponseDto toDto(AssignmentSubmission submission) {
        AssignmentSubmissionResponseDto dto = new AssignmentSubmissionResponseDto();
        dto.setId(submission.getId());
        // Убедитесь, что submission.getUser() и submission.getAssignment() загружены (JOIN FETCH в репозитории)
        dto.setUserId(submission.getUser().getId());
        dto.setAssignmentId(submission.getAssignment().getId());
        dto.setAttempts(submission.getAttempts());
        dto.setSuccessfulAttempt(submission.getSuccessfulAttempt());
        dto.setAnswer(submission.getAnswer());
        // dto.setAssignmentId(submission.getAssignment().getId()); // <-- Дублирующийся вызов, удалите эту строку
        dto.setCreatedAt(submission.getCreatedAt());
        return dto;
    }

    /**
     * Проверяет, имеет ли пользователь право просматривать конкретный сабмит.
     * Вызывает ForbiddenException, если прав недостаточно.
     *
     * @param requestUserId ID пользователя, делающего запрос.
     * @param ownerUserId   ID владельца сабмита.
     * @throws EntityNotFoundException если пользователь не найден.
     * @throws ForbiddenException      если доступ запрещен.
     */
    private void validateAccessToSubmission(Long requestUserId, Long ownerUserId) {
        User requestingUser = userRepository.findById(requestUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Пользователь может просматривать свои данные
        if (requestUserId.equals(ownerUserId)) {
            return;
        }

        // Или пользователь должен быть преподавателем/админом/владельцем
        if (requestingUser.getRole() != Role.OWNER &&
                requestingUser.getRole() != Role.ADMIN &&
                requestingUser.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Access denied: Cannot view another user's submission.");
        }
    }

    /**
     * Проверяет, имеет ли пользователь право просматривать список сабмитов.
     * Вызывает ForbiddenException, если прав недостаточно.
     *
     * @param requestUserId ID пользователя, делающего запрос.
     * @throws EntityNotFoundException если пользователь не найден.
     */
    private void validateAccessToList(Long requestUserId) {
        // Пользователь может просматривать только свой список
        userRepository.findById(requestUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    /**
     * Проверяет, имеет ли пользователь право просматривать сабмиты по заданию.
     * Вызывает ForbiddenException, если прав недостаточно.
     *
     * @param userId         ID пользователя.
     * @param assignmentId   ID задания.
     * @throws EntityNotFoundException если пользователь или задание не найдены.
     * @throws ForbiddenException      если доступ запрещен.
     */
    private void validateUserCanViewSubmissions(Long userId, Long assignmentId) {
        // Получить задание и связанный с ним курс
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Проверить роль пользователя (аналогично другим validate методам)
        if (user.getRole() != Role.OWNER &&
                user.getRole() != Role.ADMIN &&
                user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only OWNER, ADMIN, or TEACHER can view submissions for an assignment");
        }
    }
}