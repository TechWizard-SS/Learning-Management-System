package com.sstu.LearningManagementSystem.model.dto.courseRatingDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseRatingCreateDto {
    private Long courseId; // ID курса, которому ставится оценка
    private Integer rating; // Оценка (например, от 1 до 5)

    // Не включаем userId, он берется из аутентифицированного пользователя (currentUserId)
}
