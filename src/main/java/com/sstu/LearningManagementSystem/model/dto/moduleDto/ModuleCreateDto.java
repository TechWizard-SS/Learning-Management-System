package com.sstu.LearningManagementSystem.model.dto.moduleDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ModuleCreateDto {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private Long courseId;
}
