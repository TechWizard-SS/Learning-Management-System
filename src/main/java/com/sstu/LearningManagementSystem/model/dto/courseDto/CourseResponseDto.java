package com.sstu.LearningManagementSystem.model.dto.courseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CourseResponseDto {
    private Long id;
    private String name;
    private String description;
    private Integer expectedDuration;
    private Double rating;
    private Long categoryId; // ID категории
    private String categoryName; // если нужно имя
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
