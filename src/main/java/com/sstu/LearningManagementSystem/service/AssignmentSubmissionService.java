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
        AssignmentSubmission submission = assignmentSubmissionRepository
                .findByAssignmentIdAndUserId(createDto.getAssignmentId(), currentUserId)
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

        // 6. Проверить ответ (это может быть сложная логика, зависит от типа задания)
        // Пример простой проверки (например, для задания с фиксированным ответом)
        // boolean isCorrect = checkAnswer(assignment, createDto.getAnswer());
        // if (isCorrect && !submission.isPassed()) { // Если еще не сдано успешно
        //     submission.setPassed(true);
        //     submission.setSuccessfulAttempt(submission.getAttempts());
        // }

        // 7. Сохранить Submission
        AssignmentSubmission savedSubmission = assignmentSubmissionRepository.save(submission);

        // 8. Вернуть DTO
        return toDto(savedSubmission);
    }

    /**
     * Получить Submission по ID.
     * Требует проверки прав: пользователь может видеть только свой сабмит или преподаватель/админ - любой.
     *
     * @param currentUserId ID пользователя, запрашивающего сабмит.
     * @param submissionId  ID сабмита.
     * @return DTO сабмита.
     */
    public AssignmentSubmissionResponseDto getSubmissionById(Long currentUserId, Long submissionId) {
        AssignmentSubmission submission = assignmentSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found with id: " + submissionId));

        // Проверить права доступа: текущий пользователь == владелец сабмита ИЛИ преподаватель/админ
        // validateAccessToSubmission(currentUserId, submission.getUser().getId());

        return toDto(submission);
    }

    /**
     * Получить все Submission'ы пользователя по ID задания.
     * Требует проверки прав: пользователь запрашивает свои данные или преподаватель/админ.
     *
     * @param currentUserId ID пользователя, запрашивающего сабмиты.
     * @param assignmentId  ID задания.
     * @return Список DTO сабмитов.
     */
    public List<AssignmentSubmissionResponseDto> getSubmissionsByUserAndAssignmentId(Long currentUserId, Long assignmentId) {
        // Проверить права: currentUserId == запрашиваемый пользователь ИЛИ преподаватель/админ
        // validateAccessToList(currentUserId);

        // Используем метод, который возвращает List
        List<AssignmentSubmission> submissions = assignmentSubmissionRepository.findByAssignmentIdAndUserIdOrderByCreatedAtAsc(assignmentId, currentUserId);
        return submissions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить все Submission'ы для задания (например, для преподавателя).
     * Требует проверки прав: только преподаватель/админ/владелец курса.
     *
     * @param currentUserId ID пользователя, запрашивающего сабмиты.
     * @param assignmentId  ID задания.
     * @return Список DTO сабмитов.
     */
    public List<AssignmentSubmissionResponseDto> getSubmissionsByAssignmentId(Long currentUserId, Long assignmentId) {
        // Проверить права: currentUserId должен быть преподавателем/админом/владельцем курса
        validateUserCanViewSubmissions(currentUserId, assignmentId);

        List<AssignmentSubmission> submissions = assignmentSubmissionRepository.findByAssignmentId(assignmentId);
        return submissions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // --- Вспомогательные методы ---

    private AssignmentSubmissionResponseDto toDto(AssignmentSubmission submission) {
        AssignmentSubmissionResponseDto dto = new AssignmentSubmissionResponseDto();
        dto.setId(submission.getId());
        dto.setUserId(submission.getUser().getId());
        dto.setAssignmentId(submission.getAssignment().getId());
        dto.setAttempts(submission.getAttempts());
        dto.setSuccessfulAttempt(submission.getSuccessfulAttempt());
        dto.setAnswer(submission.getAnswer());
        return dto;
    }

    /**
     * Проверяет, имеет ли пользователь право просматривать сабмиты по заданию.
     * Вызывает ForbiddenException, если прав недостаточно.
     *
     * @param userId         ID пользователя.
     * @param assignmentId   ID задания.
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
        // Можно добавить дополнительную проверку: принадлежит ли задание курсу, где пользователь является преподавателем
    }
}