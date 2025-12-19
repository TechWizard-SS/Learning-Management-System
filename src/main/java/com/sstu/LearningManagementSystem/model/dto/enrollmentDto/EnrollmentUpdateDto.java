package com.sstu.LearningManagementSystem.model.dto.enrollmentDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentUpdateDto {
    // Поля, которые могут быть обновлены, например:
    private Boolean confirmed; // Преподаватель может подтвердить зачисление
    // private Integer progress; // Прогресс обычно обновляется автоматически, не через этот DTO напрямую
}