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
     private Boolean passed;
     private Integer successfulAttempt;
     private String feedback; // Поле для комментария преподавателя?
}
