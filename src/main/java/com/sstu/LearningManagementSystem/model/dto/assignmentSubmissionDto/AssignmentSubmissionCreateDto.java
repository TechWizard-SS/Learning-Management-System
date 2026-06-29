package com.sstu.LearningManagementSystem.model.dto.assignmentSubmissionDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSubmissionCreateDto {
    private Long assignmentId; // ID задания, на которое отправляется ответ
    private String answer; // Текст ответа пользователя
}
