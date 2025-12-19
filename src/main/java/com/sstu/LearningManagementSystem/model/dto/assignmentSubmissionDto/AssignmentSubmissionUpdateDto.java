package com.sstu.LearningManagementSystem.model.dto.assignmentSubmissionDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSubmissionUpdateDto {
    // Поля, которые могут быть обновлены преподавателем?
    // Например, принудительное изменение статуса passed, successfulAttempt?
    // Или только сервис сам обновляет?
    // В зависимости от бизнес-логики:
     private Boolean passed;
     private Integer successfulAttempt;
     private String feedback; // Поле для комментария преподавателя?

    // Пока оставим пустым или добавим поля по необходимости.
    // Часто сабмиты создаются и обновляются автоматически, а не вручную через этот DTO.
    // Предположим, что обновление возможно только через сервис при проверке.
}
