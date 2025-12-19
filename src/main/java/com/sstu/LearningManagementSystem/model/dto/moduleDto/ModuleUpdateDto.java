package com.sstu.LearningManagementSystem.model.dto.moduleDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ModuleUpdateDto {
    private String title;
    private String description;
}
