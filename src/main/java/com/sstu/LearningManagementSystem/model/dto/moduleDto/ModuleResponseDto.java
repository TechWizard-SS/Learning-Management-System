package com.sstu.LearningManagementSystem.model.dto.moduleDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ModuleResponseDto implements Serializable {
    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}