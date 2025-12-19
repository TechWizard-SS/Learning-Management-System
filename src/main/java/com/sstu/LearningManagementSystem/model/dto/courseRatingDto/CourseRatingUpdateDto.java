package com.sstu.LearningManagementSystem.model.dto.courseRatingDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseRatingUpdateDto {
    private Integer rating; // Новая оценка
}