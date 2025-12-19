package com.sstu.LearningManagementSystem.model.dto.courseDto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CourseCreateDto {
    @NotBlank
    private String name;
    private String description;
    private Integer expectedDuration;
    private Long categoryId; // для связи с CourseCategory
    private List<String> tags;
}