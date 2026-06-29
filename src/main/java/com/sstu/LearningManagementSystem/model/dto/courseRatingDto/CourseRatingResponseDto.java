package com.sstu.LearningManagementSystem.model.dto.courseRatingDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseRatingResponseDto {
    private Long id;
    private Long userId; // ID пользователя, поставившего оценку
    private Long courseId; // ID курса, которому поставлена оценка
    private Integer rating; // Оценка
    private LocalDateTime createdAt; // Дата создания оценки (из Auditable)
    private LocalDateTime updatedAt; // Дата последнего обновления оценки (из Auditable)
}