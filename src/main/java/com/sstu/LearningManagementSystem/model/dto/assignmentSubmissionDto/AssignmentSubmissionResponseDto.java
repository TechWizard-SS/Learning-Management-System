package com.sstu.LearningManagementSystem.model.dto.assignmentSubmissionDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSubmissionResponseDto {
    private Long id;
    private Long userId; // ID пользователя, отправившего сабмит
    private Long assignmentId; // ID задания, на которое отправлен сабмит
    private int attempts; // Количество попыток
    private Integer successfulAttempt; // Номер успешной попытки
    private Boolean passed; // Успешно ли пройдено
    private String answer; // Текст ответа
    private LocalDateTime createdAt; // Дата создания сабмита
    private LocalDateTime updatedAt; // Дата последнего обновления
    // private LocalDateTime submittedAt; // Возможное поле для даты конкретной отправки попытки?
}