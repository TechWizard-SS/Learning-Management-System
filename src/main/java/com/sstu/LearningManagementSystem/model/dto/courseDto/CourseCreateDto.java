package com.sstu.LearningManagementSystem.model.dto.courseDto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CourseCreateDto implements Serializable {
    @NotBlank
    private String name;
    private String description;
    private Integer expectedDuration;
    private Long categoryId;
    private List<String> tags;
}